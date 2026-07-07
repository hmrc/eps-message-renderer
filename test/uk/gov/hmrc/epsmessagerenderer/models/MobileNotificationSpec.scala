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

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{ Format, JsError, Json }
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.epsmessagerenderer.models.MobileNotification._

class MobileNotificationSpec extends PlaySpec {

  implicit val mobileNotificationAuditFormat: Format[MobileNotificationAudit] =
    Json.format[MobileNotificationAudit]

  "MobileNotification" must {

    "successfully read Json when identifier is a nino" in new TestCase {
      val result = ninoJson.as[MobileNotification]
      result.identifier mustBe Nino(nino)
    }

    "fail to read Json for non nino identifiers" in new TestCase {
      val result = invalidJson.validate[MobileNotification]

      result mustBe a[JsError]
    }

    "write to Json correctly" in new TestCase {
      val expectedJson = Json.parse(s"""{"identifier": {"nino": "$nino"}}""")

      Json.toJson(model) mustBe expectedJson
    }
  }

  "MobileNotificationAudit" must {
    "write to Json correctly with error" in new TestCase {
      val audit = MobileNotificationAudit(Nino(nino), 500, Some("Connection timeout"))
      val json = Json.toJson(audit)

      (json \ "identifier" \ "nino").as[String] mustBe nino
      (json \ "status").as[Int] mustBe 500
      (json \ "error").asOpt[String] mustBe Some("Connection timeout")
    }

    "write to Json correctly without error" in new TestCase {
      val audit = MobileNotificationAudit(Nino(nino), 200, None)
      val json = Json.toJson(audit)

      (json \ "status").as[Int] mustBe 200
      (json \ "error").asOpt[String] mustBe None
    }

    "read from Json correctly" in new TestCase {
      val json = Json.parse(s"""{"identifier": {"name": "nino", "value": "$nino"}, "status": 200}""")
      val result = json.as[MobileNotificationAudit]

      result.identifier mustBe Nino(nino)
      result.status mustBe 200
      result.error mustBe None
    }
  }

  "IdentifierStatus" must {
    "round trip through Json" in new TestCase {
      val json = Json.toJson(status)
      json.as[IdentifierStatus] mustBe status
    }
  }

  class TestCase {
    val nino = "AB123456C"
    val ninoJson = Json.parse(s"""{"identifier": {"name": "nino", "value": "$nino"}}""")
    val invalidJson = Json.parse("""{"identifier": {"name": "utr", "value": "12345"}}""")
    val model = MobileNotification(Nino(nino))
    val status = IdentifierStatus(isRegistered = true)
  }
}
