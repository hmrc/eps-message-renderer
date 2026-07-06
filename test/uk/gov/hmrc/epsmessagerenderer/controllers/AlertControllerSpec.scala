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

package uk.gov.hmrc.epsmessagerenderer.controllers

import org.mockito.ArgumentMatchers.{ any, eq as eqTo }
import org.mockito.Mockito.{ reset, times, verify, when }
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import play.api.test.Helpers.{ defaultAwaitTimeout, status, stubControllerComponents }
import play.api.test.{ FakeHeaders, FakeRequest }
import uk.gov.hmrc.epsmessagerenderer.models.*
import uk.gov.hmrc.epsmessagerenderer.service.PayeAlerter
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import scala.concurrent.{ ExecutionContext, Future }

class AlertControllerSpec extends PlaySpec with BeforeAndAfterEach with ScalaFutures {

  val mockService: PayeAlerter = mock[PayeAlerter]
  val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  val cc: ControllerComponents = stubControllerComponents()
  val controller = new AlertController(mockService, cc)(ec)

  override protected def beforeEach(): Unit = reset(
    mockService
  )

  val body: String =
    s"""
       |{"id":"123456",
       |"modifiedDetails":{"createdAt":"2025-05-01T13:07:02.000Z","lastUpdated":"2025-05-01T13:07:02.000Z"},
       |"failures":0,
       |"alerts":{"alert":{"identifier":{"id_type":"nino","value":"AA000003"},"hod_id":"nps","template_id":"0004"}},
       |"statusUrl":"/eps-hods-adapter/preferences/alert/print-suppression/123456/status"
       |}""".stripMargin

  val notificationRequest = FakeRequest(
    method = "POST",
    uri = "/eps-message-renderer/process-notification/",
    headers = FakeHeaders(),
    body = Json.parse(body)
  )

  val workItem = PayeNotificationWorkItem(
    "123456",
    PrintSuppressionAlert(PayePrintSuppressionNotification(Identifier("nino", "AA000003"), "nps", "0004")),
    "/eps-hods-adapter/preferences/alert/print-suppression/123456/status",
    0,
    ModifiedDetails(Instant.parse("2025-05-01T13:07:02.000Z"), Instant.parse("2025-05-01T13:07:02.000Z"))
  )

  "alert controller" must {
    "process the inbound alert notifications & set the status" in {
      when(mockService.processNotification(eqTo(workItem))(any[HeaderCarrier]))
        .thenReturn(Future.successful(true))

      val result = controller.processNotification()(notificationRequest)
      status(result) mustBe 202
      verify(mockService, times(1))
        .processNotification(any)(any[HeaderCarrier])
    }

    "process the inbound alert notifications & return the failure" in {
      when(mockService.processNotification(eqTo(workItem))(any[HeaderCarrier]))
        .thenReturn(Future.successful(false))

      val result = controller.processNotification()(notificationRequest)
      status(result) mustBe 500
      verify(mockService, times(1))
        .processNotification(any)(any[HeaderCarrier])
    }
  }
}
