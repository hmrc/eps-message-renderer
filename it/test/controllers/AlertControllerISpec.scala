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

package controllers

import com.github.tomakehurst.wiremock.client.WireMock.{ aResponse, equalTo, get, ok, post, serverError, urlEqualTo }
import play.api.Application
import play.api.http.Status.{ ACCEPTED, NO_CONTENT }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.*
import play.api.test.{ FakeHeaders, FakeRequest }
import uk.gov.hmrc.epsmessagerenderer.models.{ EmailAlert, EmailValidation, IdentifierStatus }
import uk.gov.hmrc.epsmessagerenderer.models.nps.NpsPerson
import utils.IntegrationSpec

class AlertControllerISpec extends IntegrationSpec {

  "/eps-message-renderer/process-notification" must {

    val processNotificationUrl = "/eps-message-renderer/process-notification"
    val preferencesPersonUrl = s"/eps-hods-adapter/preferences/person/$generatedNino"
    val preferencesVerifiedEmailUrl = s"/preferences/verified-email?regime=paye&taxId=${generatedNino.nino}"
    val emailUrl = "/hmrc/email"
    val mobileNotificationUrl = s"/is-registered/nino/${generatedNino.nino}"
    val mobileNotificationSendUrl = "/send/p2"

    "return an ACCEPTED response" in {
      val processNotificationBody = FakeRequest(
        method = "POST",
        uri = processNotificationUrl,
        headers = FakeHeaders(),
        body = Json.parse(
          s"""
             |{"id":"123456",
             |"modifiedDetails":{"createdAt":"2025-05-01T13:07:02.000Z","lastUpdated":"2025-05-01T13:07:02.000Z"},
             |"failures":0,
             |"alerts":{"alert":{
             |"identifier":{"id_type":"nino","value":"${generatedNino.nino}"},
             |"hod_id":"nps",
             |"template_id":"0004",
             |"parameters": {"taxYear": "2027"},
             |"notice_type": "CY_PLUS_1"}
             |},
             |"statusUrl":"/eps-hods-adapter/preferences/alert/print-suppression/123456/status"
             |}""".stripMargin
        )
      )

      val emailAlert = EmailAlert(
        List("test@digital.gov.uk"),
        "annual_tax_estimate_message_alert",
        Map("fullName" -> "Mr John Smith", "taxYear" -> "2027"),
        eventUrl = None,
        tags = Map("nino" -> s"${generatedNino.nino}", "form-type" -> "P2")
      )

      server.stubFor(
        get(urlEqualTo(preferencesPersonUrl))
          .willReturn(ok(Json.toJson(NpsPerson(generatedNino.value, "Mr", "John", "Smith")).toString))
      )

      server.stubFor(
        get(urlEqualTo(preferencesVerifiedEmailUrl))
          .willReturn(ok(Json.toJson(EmailValidation("test@digital.gov.uk")).toString))
      )

      server.stubFor(
        post(urlEqualTo(emailUrl))
          .withRequestBody(equalTo(Json.toJson(emailAlert).toString))
          .willReturn(
            aResponse()
              .withStatus(ACCEPTED)
          )
      )

      server.stubFor(
        get(urlEqualTo(mobileNotificationUrl))
          .willReturn(ok(Json.toJson(IdentifierStatus(true)).toString))
      )

      server.stubFor(
        post(urlEqualTo(mobileNotificationSendUrl))
          .willReturn(ok)
      )

      server.stubFor(
        post(urlEqualTo("/eps-hods-adapter/preferences/alert/print-suppression/123456/status"))
          .willReturn(
            aResponse()
              .withStatus(NO_CONTENT)
          )
      )

      val result = route(app, processNotificationBody)

      result.map(status) mustBe Some(ACCEPTED)
    }

    "return a INTERNAL_SERVER_ERROR response" in {
      val processNotificationBody = FakeRequest(
        method = "POST",
        uri = processNotificationUrl,
        headers = FakeHeaders(),
        body = Json.parse(
          s"""
             |{"id":"123456",
             |"modifiedDetails":{"createdAt":"2025-05-01T13:07:02.000Z","lastUpdated":"2025-05-01T13:07:02.000Z"},
             |"failures":0,
             |"alerts":{"alert":{
             |"identifier":{"id_type":"nino","value":"${generatedNino.nino}"},
             |"hod_id":"nps",
             |"template_id":"0004",
             |"parameters": {"taxYear": "2027"},
             |"notice_type": "CY_PLUS_1"}
             |},
             |"statusUrl":"/eps-hods-adapter/preferences/alert/print-suppression/123456/status"
             |}""".stripMargin
        )
      )

      server.stubFor(
        get(urlEqualTo(preferencesPersonUrl))
          .willReturn(serverError())
      )

      server.stubFor(
        post(urlEqualTo("/eps-hods-adapter/preferences/alert/print-suppression/123456/status"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      val result = route(app, processNotificationBody)

      result.map(status) mustBe Some(INTERNAL_SERVER_ERROR)
    }
  }

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(
        "microservice.services.auth.port"                                    -> server.port(),
        "microservice.services.citizen-details.port"                         -> server.port(),
        "microservice.services.hods-adapter.host"                            -> "localhost",
        "microservice.services.hods-adapter.port"                            -> server.port(),
        "microservice.services.email.port"                                   -> server.port(),
        "microservice.services.email.host"                                   -> "localhost",
        "microservice.services.mobile-push-notifications-orchestration.host" -> "localhost",
        "microservice.services.mobile-push-notifications-orchestration.port" -> server.port(),
        "microservice.services.preferences.host"                             -> "localhost",
        "microservice.services.preferences.port"                             -> server.port()
      )
      .build()
}
