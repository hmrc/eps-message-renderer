/*
 * Copyright 2023 HM Revenue & Customs
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
