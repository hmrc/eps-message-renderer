/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.connectors

import play.api.Logging
import uk.gov.hmrc.play.bootstrap.metrics.Metrics as KenshooMetrics
import play.api.http.Status.ACCEPTED
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.epsmessagerenderer.metrics.{ HasMetrics, Metrics }
import uk.gov.hmrc.epsmessagerenderer.models.EmailAlert
import uk.gov.hmrc.epsmessagerenderer.utils.Auditing
import uk.gov.hmrc.http.{ HeaderCarrier, HttpException, HttpResponse }
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.play.audit.model.EventTypes
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.StringContextOps

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class EmailConnector @Inject() (
  val metrics: KenshooMetrics,
  http: HttpClientV2,
  emailMetrics: Metrics,
  servicesConfig: ServicesConfig,
  auditing: Auditing
)(implicit ec: ExecutionContext)
    extends HasMetrics with Logging {

  private val emailServiceUrl: String = servicesConfig.baseUrl("email")

  def sendPayeAlert(emailAddress: String, taxpayersName: String, nino: Nino)(implicit
    hc: HeaderCarrier
  ): Future[Unit] = {

    val alert = EmailAlert(
      List(emailAddress),
      "tax_estimate_message_alert",
      Map("fullName" -> taxpayersName),
      eventUrl = None,
      tags = Map("nino" -> nino.nino, "form-type" -> "P2")
    )
    withMetricsTimer("send-paye-alert") { timer =>
      http
        .post(url"$emailServiceUrl/hmrc/email")
        .withBody(Json.toJson(alert))
        .execute[HttpResponse]
        .map {
          _.status match {
            case ACCEPTED =>
              timer.completeTimerAndIncrementSuccessCounter()
              emailMetrics.sentEmailCount() // Email queued
              auditing.createAudit(
                EventTypes.Succeeded,
                s"Sent email to: $emailAddress",
                nino
              )
            case s =>
              timer.completeTimerAndIncrementFailedCounter()
              auditing.createAudit(
                EventTypes.Failed,
                s"Failed to send email to: $emailAddress | returned $s",
                nino
              )
              throw new HttpException(s"Unexpected response ($s) from email service", s)
          }
        } recover { case ex =>
        timer.completeTimerAndIncrementFailedCounter()
        logger.error(
          s"Problem occurred while sending paye alert for ${nino.nino} due to ${ex.getMessage}"
        )
        auditing.createAudit(
          EventTypes.Failed,
          s"Failed to send email due to ${ex.getMessage}",
          nino
        )
        throw new Exception(ex.getMessage, ex)
      }
    }
  }
}
