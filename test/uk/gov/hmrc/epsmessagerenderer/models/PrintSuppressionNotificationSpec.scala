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
      auditDetails("availableAt") mustBe now.toString()
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
}
