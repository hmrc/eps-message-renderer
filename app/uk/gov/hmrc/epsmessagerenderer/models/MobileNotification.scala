/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.models

import play.api.libs.json._
import play.api.libs.functional.syntax._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.domain.TaxIds.TaxIdWithName

case class MobileNotification(identifier: TaxIdWithName)
case class MobileNotificationAudit(identifier: TaxIdWithName, status: Int, error: Option[String] = None)

object MobileNotification {

  implicit val identifierReads: Reads[TaxIdWithName] =
    ((__ \ "name").readNullable[String] and (__ \ "value")
      .readNullable[String]).tupled
      .flatMap[TaxIdWithName] {
        case (Some("nino"), Some(value)) =>
          Reads[TaxIdWithName] { _ =>
            JsSuccess(Nino(value))
          }
        case (id, value) =>
          Reads[TaxIdWithName] { _ =>
            JsError(s"It is not required to send the mobile notification for $id : $value")
          }
      }

  implicit val identifierWrites: Writes[TaxIdWithName] =
    new Writes[TaxIdWithName] {
      override def writes(taxId: TaxIdWithName): JsValue =
        JsObject(Seq(taxId.name -> JsString(taxId.value)))
    }
  implicit val format: Format[TaxIdWithName] =
    Format(identifierReads, identifierWrites)

  implicit val mobileNotificationFormats: Format[MobileNotification] =
    Json.format[MobileNotification]
}

case class IdentifierStatus(isRegistered: Boolean)

object IdentifierStatus {
  implicit val identifierStatusFormat: Format[IdentifierStatus] =
    Json.format[IdentifierStatus]
}
