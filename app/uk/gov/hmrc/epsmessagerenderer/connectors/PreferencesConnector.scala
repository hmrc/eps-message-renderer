/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.connectors

import play.api.Logging

import javax.inject.Inject
import play.api.http.Status.{ NOT_FOUND, OK }
import uk.gov.hmrc.domain.TaxIdentifier
import uk.gov.hmrc.epsmessagerenderer.metrics.HasMetrics
import uk.gov.hmrc.play.bootstrap.metrics.Metrics as KenshooMetrics
import uk.gov.hmrc.epsmessagerenderer.models.{ EmailValidation, VerifiedEmailAddressResponse, VerifiedEmailError, VerifiedEmailNotFound }
import uk.gov.hmrc.epsmessagerenderer.utils.Auditing
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{ HeaderCarrier, HttpReads, HttpResponse }
import uk.gov.hmrc.play.audit.model.EventTypes
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URI
import scala.concurrent.{ ExecutionContext, Future }

class PreferencesConnector @Inject() (
  val metrics: KenshooMetrics,
  http: HttpClientV2,
  servicesConfig: ServicesConfig,
  auditing: Auditing
) extends HasMetrics with Logging {

  def baseUrl: String = servicesConfig.baseUrl("preferences")

  def getVerifiedEmailAddress(
    identifier: TaxIdentifier
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[VerifiedEmailAddressResponse] = {
    val preferencesUrl =
      URI(s"$baseUrl/preferences/verified-email?regime=paye&taxId=${identifier.value}").toURL
    withMetricsTimer("get-verified-email-address") { timer =>
      http
        .get(preferencesUrl)
        .execute[HttpResponse]
        .map { response =>
          logger.info(preferencesUrl.toString + " called to entity-resolver")
          response.status match {
            case OK =>
              logger.info(s"$preferencesUrl Api given OK response $response")
              timer.completeTimerAndIncrementSuccessCounter()
              val result = response.json.as[EmailValidation]
              auditing.createAudit[EmailValidation](
                EventTypes.Succeeded,
                s"Verified email address found for $identifier",
                result
              )
              result
            case NOT_FOUND =>
              logger.warn(s"$preferencesUrl Api given NOT FOUND response $response")
              timer.completeTimerAndIncrementFailedCounter()
              val result =
                VerifiedEmailNotFound(response.body)
              auditing.createAudit[VerifiedEmailNotFound](
                EventTypes.Failed,
                s"Verified email address not found for $identifier",
                result
              )
              result
            case status =>
              timer.completeTimerAndIncrementFailedCounter()
              val reason = s"Call returned $status"
              auditing.createAudit[VerifiedEmailError](
                EventTypes.Failed,
                s"Unable to verify email address for $identifier | $reason",
                VerifiedEmailError(reason)
              )
              throw new OtherException(s"OTHER_EXCEPTION_$status")
          }
        } recover { case e: Exception =>
        val reason = s"Call threw exception: ${e.getClass}"
        logger.warn(
          "[getVerifiedEmailAddress] , Problem occurred while getting verified email address for NINO " + s"$identifier | $reason"
        )
        timer.completeTimerAndIncrementFailedCounter()
        auditing.createAudit[VerifiedEmailError](
          EventTypes.Failed,
          s"Unable to verify email address for $identifier | $reason",
          VerifiedEmailError(reason)
        )
        throw new Exception(e.getMessage, e)
      }
    }
  }

  // Implicit reads for Read the raw response
  implicit val httpReads: HttpReads[HttpResponse] =
    new HttpReads[HttpResponse] {
      override def read(method: String, url: String, response: HttpResponse) =
        response
    }
}

class OtherException(message: String) extends Exception(message)
