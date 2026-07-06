/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.models

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{ JsString, Json }

import java.time.Instant

class EmailEventsSpec extends PlaySpec {

  "MailgunEventType" must {
    """be successfully deserialized from string "PermanentBounce" """ in {
      JsString("PermanentBounce").as[MailgunEventType] must be(PermanentBounce)
    }
    """be successfully deserialized from string "Anyting Else" """ in {
      JsString("Anything Else").as[MailgunEventType] must be(Other)
    }
  }

  "EmailEvent" must {
    val sampleDate: Instant = Instant.ofEpochMilli(1436434662989L)

    " be successfully deserialized from the serialized PermanentBounce type" in {
      Json
        .parse(""" { "event":"PermanentBounce", "detected":"2015-07-09T09:37:42.989Z" } """)
        .as[EmailEvent] must be(EmailEvent(PermanentBounce, sampleDate))
    }
    " be successfully deserialized from the serialized non PermanentBounce type" in {
      Json
        .parse(""" { "event":"other type", "detected":"2015-07-09T09:37:42.989Z" } """)
        .as[EmailEvent] must be(EmailEvent(Other, sampleDate))
    }
  }
}
