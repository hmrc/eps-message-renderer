/*
 * Copyright 2026 HM Revenue & Customs
 *
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
