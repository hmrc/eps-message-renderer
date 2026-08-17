/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.epsmessagerenderer.service

import play.api.Logger
import play.api.http.Status.*
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.epsmessagerenderer.connectors.*
import uk.gov.hmrc.epsmessagerenderer.metrics.Metrics
import uk.gov.hmrc.epsmessagerenderer.models.*
import uk.gov.hmrc.epsmessagerenderer.models.nps.{ NpsPerson, PersonResult, SalutationHelper }
import uk.gov.hmrc.epsmessagerenderer.models.workitem.ProcessingStatus
import uk.gov.hmrc.epsmessagerenderer.models.workitem.ProcessingStatus.*
import uk.gov.hmrc.epsmessagerenderer.utils.Auditing
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.model.EventTypes

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

class PayeAlerter @Inject() (
  hodsAdapterConnector: HodsAdapterConnector,
  preferencesConnector: PreferencesConnector,
  emailConnector: EmailConnector,
  mobileConnector: MobileConnector,
  metrics: Metrics,
  auditing: Auditing
)(implicit ec: ExecutionContext) {

  val logger: Logger = Logger(this.getClass)

  def processNotification(notification: PayeNotificationWorkItem)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val nino = notification.alerts.alert.identifier.value.trim
    val ninoWithTempSuffix = ninoFromInputStringOrAppendTempSuffix(nino)

    getPersonDetails(ninoWithTempSuffix).flatMap {
      case PersonResult(OK, Some(person)) => verifyEmailAndSendPayeAlert(notification, person)

      case PersonResult(NOT_FOUND, None) =>
        hodsAdapterConnector.optUserOutOfPrintSuppression(ninoWithTempSuffix.nino)
        setStatus(PermanentlyFailed, notification.statusUrl, None)

      case _ => setStatus(Failed, notification.statusUrl, None)
    }
  }

  private def verifyEmailAndSendPayeAlert(notification: PayeNotificationWorkItem, person: NpsPerson)(implicit
    hc: HeaderCarrier
  ) =
    getVerifiedEmailAddress(Nino(person.nino)).flatMap {
      case Some(emailAddress) =>
        emailConnector.sendPayeAlert(
          emailAddress,
          SalutationHelper.salutationFrom(NpsPerson.getTaxpayersName(person)),
          Nino(person.nino),
          templateIdForEmailAlert(notification),
          additionalParameterForEmailAlert(notification)
        ) flatMap { _ =>
          mobileConnector.checkAndSendNotification(Nino(person.nino))
          setStatus(Succeeded, notification.statusUrl, None)
        }

      case None =>
        hodsAdapterConnector.optUserOutOfPrintSuppression(person.nino)
        setStatus(PermanentlyFailed, notification.statusUrl, None)
    }

  private def npsGetPersonResponseTimer(startTime: Long): Unit = metrics.npsGetPersonResponseTimer(
    System.currentTimeMillis - startTime,
    TimeUnit.MILLISECONDS
  )

  private def getPersonDetails(nino: Nino)(implicit
    hc: HeaderCarrier
  ): Future[PersonResult] = {
    val getPersonStartTime = System.currentTimeMillis
    hodsAdapterConnector
      .getPerson(nino)
      .map { response =>
        response.status match {
          case OK =>
            npsGetPersonResponseTimer(getPersonStartTime)
            val person = Try(response.json.as[NpsPerson]).toOption

            auditing.createAudit[Nino](
              EventTypes.Succeeded,
              "GET Person details succeeded",
              Nino(person.fold(nino.nino)(_.nino))
            )
            PersonResult(OK, person)
          case statusCode =>
            npsGetPersonResponseTimer(getPersonStartTime)
            auditing.createAudit[Nino](EventTypes.Failed, s"GET Person details $statusCode", nino)
            PersonResult(statusCode, None)
        }
      } recover { case ex =>
      auditing.createAudit[Nino](EventTypes.Failed, s"GET Person details failed with exception ${ex.getMessage}", nino)
      logger.warn(
        s"GET of user details from NPS failed for user ${nino.nino} due to ${ex.getMessage}"
      )
      PersonResult(INTERNAL_SERVER_ERROR, None)
    }
  }

  // ToDo Remove this wrapper function & use connector function with returning emailAddress
  private def getVerifiedEmailAddress(nino: Nino)(implicit
    hc: HeaderCarrier
  ): Future[Option[String]] =
    preferencesConnector
      .getVerifiedEmailAddress(nino)
      .map {
        case EmailValidation(emailAddress) => Option(emailAddress)
        case _                             => None
      } recover { case ex =>
      auditing.createAudit[Nino](EventTypes.Failed, s"Verified Email call returned an exception ${ex.getMessage}", nino)
      logger.warn(
        s"Error getting emailAddress from preferences for user ${nino.nino} due to ${ex.getMessage}"
      )
      None
    }

  private def templateIdForEmailAlert(notification: PayeNotificationWorkItem): String =
    notification.alerts.alert.notice_type.fold("tax_estimate_message_alert") { noticeType =>
      noticeType.trim.toLowerCase match {
        case "cy" => "daily_tax_estimate_message_alert"
        case _    => "annual_tax_estimate_message_alert"
      }
    }

  private def additionalParameterForEmailAlert(notification: PayeNotificationWorkItem): Option[String] =
    notification.alerts.alert.parameters.map(_.taxYear)

  def setStatus(status: ProcessingStatus, statusUrl: String, deferral: Option[Instant] = None)(implicit
    hc: HeaderCarrier
  ): Future[Boolean] =
    hodsAdapterConnector
      .setWorkItemStatus(statusUrl, NotificationStatus(status, deferral))
}
