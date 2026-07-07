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
