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

package uk.gov.hmrc.epsmessagerenderer.utils

import play.api.Logging

import javax.inject.Inject
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.epsmessagerenderer.models._
import uk.gov.hmrc.epsmessagerenderer.models.nps.NpsPrintSuppressionUpdateRequest
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.{ DataEvent, EventTypes }

import scala.concurrent.ExecutionContext

class Auditing @Inject() (auditConnector: AuditConnector) extends Logging {

  def createAudit[A](auditType: String, message: String, auditItem: A)(implicit ec: ExecutionContext): Unit =
    auditType match {
      case EventTypes.Succeeded | EventTypes.Failed =>
        createAuditEvent(auditType, message, auditItem)
      case _ => logger.info("Cant log event, no valid status passed")
    }

  private def createDataEvent[A](auditType: String, message: String, a: A): DataEvent = {

    val auditSource = "eps-message-renderer"
    a match {
      case emailValidation: EmailValidation =>
        DataEvent(
          auditSource,
          auditType,
          tags = Map(s"Email Validation:" -> message),
          detail = Map("email" -> emailValidation.email)
        )
      case emailNotFound: VerifiedEmailNotFound =>
        DataEvent(
          auditSource,
          auditType,
          tags = Map(s"Email Not Found:" -> message),
          detail = Map("reason" -> emailNotFound.getMessage)
        )
      case emailError: VerifiedEmailError =>
        DataEvent(
          auditSource,
          auditType,
          tags = Map(s"Email Error:" -> message),
          detail = Map("reason" -> emailError.reason)
        )
      case notificationStatus: NotificationStatus =>
        DataEvent(
          auditSource,
          auditType,
          tags = Map(s"Notification status:" -> message),
          detail = notificationStatus.getAuditingDetails
        )
      case nino: Nino =>
        DataEvent(
          auditSource,
          auditType,
          tags = Map("transactionName" -> message),
          detail = Map("Nino" -> nino.nino)
        )
      case printSuppression: NpsPrintSuppressionUpdateRequest =>
        DataEvent(
          auditSource,
          auditType,
          tags = Map(s"Print Suppression Update Request" -> message),
          detail = printSuppression.getAuditingDetails
        )
      case mn: MobileNotificationAudit =>
        DataEvent(
          auditSource,
          auditType,
          tags = Map(s"transactionName" -> message),
          detail = Map(
            "identifier" -> mn.identifier.name,
            "value"      -> mn.identifier.value,
            "status"     -> mn.status.toString
          ) ++ mn.error.fold(Map.empty[String, String])(e => Map("error" -> e))
        )
      case other =>
        throw new IllegalArgumentException(s"The given type ${other.getClass} is not supported for auditing")
    }
  }

  private def createAuditEvent[A](auditType: String, message: String, item: A)(implicit ec: ExecutionContext): Unit = {
    val event = createDataEvent[A](auditType, message, item)
    logger.info(s"Auditing DataEvent:  $event")
    auditConnector.sendEvent(event)
  }
}
