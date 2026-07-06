/*
 * Copyright 2023 HM Revenue & Customs
 *
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
