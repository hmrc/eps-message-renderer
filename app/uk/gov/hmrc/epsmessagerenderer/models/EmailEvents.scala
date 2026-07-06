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
import uk.gov.hmrc.epsmessagerenderer.utils.DateFormats

import java.time.Instant

sealed trait MailgunEventType

case object Other extends MailgunEventType

case object PermanentBounce extends MailgunEventType

object MailgunEventType {

  implicit val mailgunEventTypeReader: Reads[MailgunEventType] =
    new Reads[MailgunEventType] {
      override def reads(json: JsValue): JsResult[MailgunEventType] =
        json match {
          case JsString("PermanentBounce") => JsSuccess(PermanentBounce)
          case _                           => JsSuccess(Other)
        }
    }
}

case class EmailEvent(event: MailgunEventType, detected: Instant)

object EmailEvent {
  implicit val instantFormats: Format[Instant] = DateFormats.instantFormats
  implicit val readEmailEventType: Reads[MailgunEventType] =
    MailgunEventType.mailgunEventTypeReader
  implicit val readEmailEvent: Reads[EmailEvent] = Json.reads[EmailEvent]
}

case class EmailEvents(events: Seq[EmailEvent])

object EmailEvents {
  implicit val reads: Reads[EmailEvents] = Json.reads[EmailEvents]
}
