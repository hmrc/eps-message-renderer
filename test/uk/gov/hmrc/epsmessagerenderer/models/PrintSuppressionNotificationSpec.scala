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

import play.api.libs.json.Json
import uk.gov.hmrc.epsmessagerenderer.BaseSpec
import uk.gov.hmrc.epsmessagerenderer.models.workitem.ProcessingStatus.Succeeded
import uk.gov.hmrc.epsmessagerenderer.utils.DateFormats

import java.time.Instant

class PrintSuppressionNotificationSpec extends BaseSpec {

  val now: Instant = Instant.ofEpochMilli(System.currentTimeMillis())

  "NotificationStatus" must {

    "write a DateTime object to json" in {

      val notificationStatus = NotificationStatus(Succeeded, Some(now))
      val jsonNotificationStatus = Json.parse(
        s"""
           |{
           |   "status": "succeeded",
           |   "availableAt": ${DateFormats.instantFormats.writes(now)}
           |}
        """.stripMargin
      )

      Json.toJson(notificationStatus) mustBe jsonNotificationStatus

    }

    "read from json with availableAt present" in {
      val json = Json.parse(
        s"""
           |{
           |   "status": "succeeded",
           |   "availableAt": ${DateFormats.instantFormats.writes(now)}
           |}
        """.stripMargin
      )

      val result = json.as[NotificationStatus]
      result.status mustBe Succeeded
      result.availableAt mustBe Some(now)
    }

    "return auditing details with availableAt" in {
      val notificationStatus = NotificationStatus(Succeeded, Some(now))
      val auditDetails = notificationStatus.getAuditingDetails

      auditDetails("status") mustBe "succeeded"
      auditDetails("availableAt") mustBe now.toString
    }

    "return N/A in auditing details when availableAt is None" in {
      val notificationStatus = NotificationStatus(Succeeded, None)
      val auditDetails = notificationStatus.getAuditingDetails

      auditDetails("availableAt") mustBe "N/A"
    }
  }

  "PayeNotificationWorkItem" must {

    "have the isUnprocessed variable set to true" in {
      val workItem = PayeNotificationWorkItem(
        "123456",
        PrintSuppressionAlert(PayePrintSuppressionNotification(Identifier("nino", "AB123456C"), "nps", "P2")),
        "status",
        0,
        ModifiedDetails(now, now)
      )
      workItem.isUnprocessed mustBe true
    }

    "return correct auditing details" in {
      val workItem = PayeNotificationWorkItem(
        "123456",
        PrintSuppressionAlert(PayePrintSuppressionNotification(Identifier("nino", "AB123456C"), "nps", "P2")),
        "status",
        0,
        ModifiedDetails(now, now)
      )

      val auditDetails = workItem.getAuditingDetail

      auditDetails("id") mustBe "AB123456C"
      auditDetails("IdType") mustBe "nino"
      auditDetails("alertId") mustBe "123456"
      auditDetails("hodId") mustBe "nps"
      auditDetails("templateId") mustBe "P2"
    }
  }

  "AlertParameter.format" should {
    import AlertParameter.format

    "read the json correctly" in new TestCase {
      Json.parse(alertParameterJsonString).as[AlertParameter] mustBe alertParameterOb
    }

    "throw exception for incorrect json" in new TestCase {
      intercept[RuntimeException] {
        Json.parse(invalidAlertParameterJsonString).as[AlertParameter]
      }
    }

    "write the json correctly" in new TestCase {
      Json.toJson(alertParameterOb) mustBe Json.parse(alertParameterJsonString)
    }
  }

  "PayePrintSuppressionNotification.printSuppressionNotificationFormat" must {

    import PayePrintSuppressionNotification.printSuppressionNotificationFormat

    "read the json correctly" in new TestCase {
      Json
        .parse(payePrintSupNotifWithNoNoticeTypeAndAlertParameterJsonString)
        .as[PayePrintSuppressionNotification] mustBe payePrintSupNotifWithNoNoticeTypeAndParamsOb

      Json
        .parse(payePrintSupNotifWithNoticeTypeAndAlertParameterJsonString)
        .as[PayePrintSuppressionNotification] mustBe payePrintSupNotifWithNoticeTypeAndParamsOb
    }

    "throw exception for invalid json" in new TestCase {
      intercept[RuntimeException] {
        Json.parse(invalidPayePrintSupNotifJsonString).as[PayePrintSuppressionNotification]
      }
    }

    "write the object correctly" in new TestCase {
      Json.toJson(payePrintSupNotifWithNoNoticeTypeAndParamsOb) mustBe Json.parse(
        payePrintSupNotifWithNoNoticeTypeAndAlertParameterJsonString
      )

      Json.toJson(payePrintSupNotifWithNoticeTypeAndParamsOb) mustBe Json.parse(
        payePrintSupNotifWithNoticeTypeAndAlertParameterJsonString
      )
    }
  }

  "PrintSuppressionAlert.printSuppressionAlertFormat" must {

    import PrintSuppressionAlert.printSuppressionAlertFormat

    "read the json correctly" in new TestCase {
      Json.parse(printSuppressionAlertJsonString).as[PrintSuppressionAlert] mustBe printSupAlertOb
      Json
        .parse(printSupAlertWithNoNoticeTypeAndParamsJsonString)
        .as[PrintSuppressionAlert] mustBe printSupAlertWithNoNoticeTypeAndParamsOb
    }

    "throw exception for invalid json" in new TestCase {
      intercept[RuntimeException] {
        Json.parse(invalidPrintSupAlertJsonString).as[PrintSuppressionAlert]
      }
    }

    "write the object correctly" in new TestCase {
      Json.toJson(printSupAlertOb) mustBe Json.parse(printSuppressionAlertJsonString)
      Json.toJson(printSupAlertWithNoNoticeTypeAndParamsOb) mustBe Json.parse(
        printSupAlertWithNoNoticeTypeAndParamsJsonString
      )
    }
  }

  trait TestCase {
    val alertParameterJsonString = """{"taxYear":"2026"}"""
    val invalidAlertParameterJsonString = """{"year":"2026"}"""
    val alertParameterOb = AlertParameter("2026")

    val identifierJsonString = """{"id_type":"nino", "value":"AA000003"}"""
    val invalidIdentifierJsonString = """{"id_type":"nino"}"""
    val identifierOb = Identifier("nino", "AA000003")

    val payePrintSupNotifWithNoNoticeTypeAndAlertParameterJsonString: String =
      """{
        |"identifier":{"id_type":"nino", "value":"AA000003"},
        |"hod_id": "nps",
        |"template_id":"4"
        |}""".stripMargin

    val payePrintSupNotifWithNoticeTypeAndAlertParameterJsonString: String =
      """{
        |"identifier":{"id_type":"nino", "value":"AA000003"},
        |"hod_id": "nps",
        |"template_id":"4",
        |"notice_type": "CY_PLUS_1",
        |"parameters":{"taxYear": "2026"}
        |}""".stripMargin

    val invalidPayePrintSupNotifJsonString: String =
      """{
        |"identifier":{"id_type":"nino", "value":"AA000003"},
        |"template_id":"4"
        |}""".stripMargin

    val payePrintSupNotifWithNoNoticeTypeAndParamsOb =
      PayePrintSuppressionNotification(identifier = identifierOb, hod_id = "nps", template_id = "4")

    val payePrintSupNotifWithNoticeTypeAndParamsOb =
      PayePrintSuppressionNotification(
        identifier = identifierOb,
        hod_id = "nps",
        template_id = "4",
        notice_type = Some("CY_PLUS_1"),
        parameters = Some(alertParameterOb)
      )

    val printSuppressionAlertJsonString: String =
      """{
        |"alert": {
        |"identifier":{"id_type":"nino", "value":"AA000003"},
        |"hod_id": "nps",
        |"template_id":"4",
        |"notice_type": "CY_PLUS_1",
        |"parameters":{"taxYear": "2026"}
        |}
        |}""".stripMargin

    val printSupAlertWithNoNoticeTypeAndParamsJsonString: String =
      """{
        |"alert": {
        |"identifier":{"id_type":"nino", "value":"AA000003"},
        |"hod_id": "nps",
        |"template_id":"4"
        |}
        |}""".stripMargin

    val invalidPrintSupAlertJsonString = """{}"""

    val printSupAlertOb = PrintSuppressionAlert(payePrintSupNotifWithNoticeTypeAndParamsOb)
    val printSupAlertWithNoNoticeTypeAndParamsOb = PrintSuppressionAlert(payePrintSupNotifWithNoNoticeTypeAndParamsOb)
  }
}
