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

package uk.gov.hmrc.epsmessagerenderer.models

import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import uk.gov.hmrc.epsmessagerenderer.models.workitem.ProcessingStatus
import uk.gov.hmrc.epsmessagerenderer.utils.DateFormats

import java.time.Instant

case class NotificationStatus(status: ProcessingStatus, availableAt: Option[Instant] = None) {

  def getAuditingDetails: Map[String, String] = Map(
    "status"      -> status.name,
    "availableAt" -> availableAt.fold("N/A")(_.toString())
  )
}

object NotificationStatus {

  implicit val dateTimeFormats: Format[Instant] = DateFormats.instantFormats
  implicit val statusFormats: Format[ProcessingStatus] = ProcessingStatus.format
  implicit val format: OFormat[NotificationStatus] =
    Json.format[NotificationStatus]
}

case class ModifiedDetails(createdAt: Instant, lastUpdated: Instant)

object ModifiedDetails {
  implicit val dateFormats: Format[Instant] = DateFormats.instantFormats
  implicit val md: OFormat[ModifiedDetails] = Json.format[ModifiedDetails]

}

case class AlertParameter(taxYear: String)

object AlertParameter {
  implicit val format: OFormat[AlertParameter] = Json.format[AlertParameter]
}

case class Identifier(id_type: String, value: String)

object Identifier {
  implicit val identifierFormat: OFormat[Identifier] = Json.format[Identifier]
}

case class PayePrintSuppressionNotification(
  identifier: Identifier,
  hod_id: String,
  template_id: String,
  notice_type: Option[String] = None,
  parameters: Option[AlertParameter] = None
)

object PayePrintSuppressionNotification {
  implicit val printSuppressionNotificationFormat: OFormat[PayePrintSuppressionNotification] =
    Json.format[PayePrintSuppressionNotification]
}

case class PrintSuppressionAlert(alert: PayePrintSuppressionNotification)

object PrintSuppressionAlert {
  implicit val printSuppressionAlertFormat: OFormat[PrintSuppressionAlert] =
    Json.format[PrintSuppressionAlert]
}

object PayeNotificationWorkItem {

  implicit val workItemReads: Reads[PayeNotificationWorkItem] =
    (
      (__ \ "id").read[String] and
        (__ \ "alerts").read[PrintSuppressionAlert] and
        (__ \ "statusUrl").read[String] and
        (__ \ "failures").read[Int] and
        (__ \ "modifiedDetails").read[ModifiedDetails]
    )(PayeNotificationWorkItem.apply)
}

case class PayeNotificationWorkItem(
  notificationId: String,
  alerts: PrintSuppressionAlert,
  statusUrl: String,
  failures: Int,
  modifiedDetails: ModifiedDetails
) {
  def isUnprocessed = true

  def getAuditingDetail: Map[String, String] = Map(
    "id"         -> alerts.alert.identifier.value,
    "IdType"     -> alerts.alert.identifier.id_type,
    "alertId"    -> notificationId,
    "hodId"      -> alerts.alert.hod_id,
    "templateId" -> alerts.alert.template_id
  )
}
