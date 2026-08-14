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

package uk.gov.hmrc.epsmessagerenderer.connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import org.mockito.Mockito.{ reset, times, verify }
import play.api.Application
import play.api.http.Status.{ ACCEPTED, BAD_REQUEST }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.epsmessagerenderer.BaseSpec
import uk.gov.hmrc.epsmessagerenderer.models.EmailAlert

class EmailConnectorSpec extends BaseSpec with WireMockHelper {

  override lazy val app: Application = GuiceApplicationBuilder()
    .configure(
      Map(
        "microservice.services.email.port" -> server.port,
        "auditing.enabled"                 -> false
      )
    )
    .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockMetrics)
  }

  def sut: EmailConnector =
    new EmailConnector(
      kenshooMetrics,
      http,
      mockMetrics,
      servicesConfig,
      auditing
    )
  val templateId: String = "tax_estimate_message_alert"
  val emailAddress: String = "test@test.com"
  val taxpayersName: String = "Joe Bloggs"

  "EmailConnector" must {

    val sendPayeAlertUrl: String = "/hmrc/email"

    "send an email alert" when {

      "the http call returns an ACCEPTED (202) status and template id is default one" in {

        server.stubFor(
          post(sendPayeAlertUrl)
            .willReturn(aResponse.withStatus(ACCEPTED))
        )

        await(sut.sendPayeAlert(emailAddress, taxpayersName, nino))
        verify(mockMetrics).sentEmailCount()

        val emailAlert = EmailAlert(
          List(emailAddress),
          "tax_estimate_message_alert",
          Map("fullName" -> taxpayersName),
          eventUrl = None,
          tags = Map("nino" -> nino.nino, "form-type" -> "P2")
        )

        server.verify(
          postRequestedFor(urlEqualTo(sendPayeAlertUrl))
            .withRequestBody(equalTo(Json.toJson(emailAlert).toString))
        )

      }

      "the http call returns an ACCEPTED (202) status and template id is provided" in {
        val templateId = "daily_tax_estimate_message_alert"
        val emailAlert = EmailAlert(
          List(emailAddress),
          templateId,
          Map("fullName" -> taxpayersName),
          eventUrl = None,
          tags = Map("nino" -> nino.nino, "form-type" -> "P2")
        )

        server.stubFor(
          post(sendPayeAlertUrl)
            .withRequestBody(matchingJsonPath("$.templateId", equalTo(templateId)))
            .withRequestBody(matchingJsonPath("$.to", equalTo("test@test.com")))
            .withRequestBody(matchingJsonPath("$.parameters.fullName", equalTo("Joe Bloggs")))
            .withRequestBody(matchingJsonPath("$.tags.form-type", equalTo("P2")))
            .willReturn(aResponse.withStatus(ACCEPTED))
        )

        await(sut.sendPayeAlert(emailAddress, taxpayersName, nino, templateId))
        verify(mockMetrics).sentEmailCount()

        server.verify(
          postRequestedFor(urlEqualTo(sendPayeAlertUrl))
            .withRequestBody(equalTo(Json.toJson(emailAlert).toString))
        )

      }
    }

    "throw an exception and not send an email alert" when {

      "the http call returns any other status" in {

        server.stubFor(
          post(sendPayeAlertUrl)
            .willReturn(aResponse.withStatus(BAD_REQUEST))
        )

        val e = intercept[Exception] {
          await(sut.sendPayeAlert(emailAddress, taxpayersName, nino))
        }

        e.getMessage must include(s"Unexpected response ($BAD_REQUEST) from email service")

        verify(mockMetrics, times(0)).sentEmailCount()
      }

      "the http call throws an exception" in {

        server.stubFor(
          post(sendPayeAlertUrl)
            .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE))
        )

        intercept[Exception] {
          await(sut.sendPayeAlert(emailAddress, taxpayersName, nino))
        }

        verify(mockMetrics, times(0)).sentEmailCount()
      }
    }
  }
}
