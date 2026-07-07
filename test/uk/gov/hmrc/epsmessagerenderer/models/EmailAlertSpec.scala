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
import play.api.libs.json.{ Json, Reads }

class EmailAlertSpec extends PlaySpec {

  "EmailAlert" must {
    "write to Json with all fields present " in {
      val emailAlert = EmailAlert(
        to = List("test@example.com"),
        templateId = "template_id",
        parameters = Map("name" -> "Name"),
        force = Some(true),
        eventUrl = Some("http://event.com"),
        tags = Map("type" -> "alert")
      )

      val expectedJson = Json.parse("""{
          "to": ["test@example.com"],
          "templateId": "template_id",
          "parameters": {"name": "Name"},
          "force": true,
          "eventUrl": "http://event.com",
          "tags": {"type": "alert"}
        }""")

      Json.toJson(emailAlert) mustBe expectedJson
    }

    "write to Json with only mandatory fields" in {
      val emailAlert = EmailAlert(
        to = List("test@example.com"),
        templateId = "template_id",
        parameters = Map("name" -> "Name")
      )

      val json = Json.toJson(emailAlert)

      (json \ "to").as[Seq[String]] mustBe Seq("test@example.com")
      (json \ "force").asOpt[Boolean] mustBe None
      (json \ "eventUrl").asOpt[String] mustBe None
      (json \ "tags").as[Map[String, String]] mustBe Map.empty
    }
  }
}
