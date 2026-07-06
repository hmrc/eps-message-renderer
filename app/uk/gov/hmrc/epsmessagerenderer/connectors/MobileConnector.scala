/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.connectors

import uk.gov.hmrc.http.HttpReads.Implicits.*
import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.epsmessagerenderer.models.{ IdentifierStatus, MobileNotification, MobileNotificationAudit }
import uk.gov.hmrc.epsmessagerenderer.utils.Auditing
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse, StringContextOps }
import uk.gov.hmrc.play.audit.model.EventTypes
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class MobileConnector @Inject() (http: HttpClientV2, servicesConfig: ServicesConfig, auditing: Auditing)
    extends Logging {

  def baseUrl: String =
    servicesConfig.baseUrl("mobile-push-notifications-orchestration")

  def checkAndSendNotification(nino: Nino)(implicit headerCarrier: HeaderCarrier, ex: ExecutionContext): Future[Unit] =
    http
      .get(url"$baseUrl/is-registered/nino/${nino.value}")
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case OK if response.json.as[IdentifierStatus].isRegistered =>
            logger.info(s"$nino is registered")
            sendNotification(MobileNotification(nino))

          case OK =>
            logger.info(s"$nino is not registered")
            auditing.createAudit[MobileNotificationAudit](
              EventTypes.Failed,
              s"Mobile Notification",
              MobileNotificationAudit(nino, OK, Some(s"$nino is not registered"))
            )
            Future.successful(())

          case anyOtherStatus =>
            auditing.createAudit[MobileNotificationAudit](
              EventTypes.Failed,
              s"Mobile Notification",
              MobileNotificationAudit(nino, anyOtherStatus, Some(s"Invalid Request sent for $nino"))
            )
            Future.successful(())
        }
      }
      .recover { case e =>
        logger.warn(s"Error while checking the registered status for $nino. Error: ${e.getMessage}")
        auditing
          .createAudit[Throwable](EventTypes.Failed, "Mobile Notification", e)
      }

  def sendNotification(
    mn: MobileNotification
  )(implicit headerCarrier: HeaderCarrier, ex: ExecutionContext): Future[Unit] =
    http
      .post(url"$baseUrl/send/p2")
      .withBody(Json.toJson(mn))
      .execute[HttpResponse]
      .map { status =>
        logger.info(s"Successfully published the mobile notification for ${mn.identifier} with status ${status.status}")
        auditing.createAudit[MobileNotificationAudit](
          EventTypes.Succeeded,
          s"Mobile Notification",
          MobileNotificationAudit(mn.identifier, status.status)
        )
      }
      .recover { case e =>
        logger.warn(s"Error while attempting to push the notification for ${mn.identifier}. Error:${e.getMessage}")
        auditing
          .createAudit[Throwable](EventTypes.Failed, "Mobile Notification", e)
      }
}
