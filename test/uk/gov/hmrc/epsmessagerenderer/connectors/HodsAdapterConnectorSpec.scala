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

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.scalatest.concurrent.IntegrationPatience
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.*
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.epsmessagerenderer.BaseSpec
import uk.gov.hmrc.epsmessagerenderer.fixtures.NpsPersonFixture.getValidPersonObject
import uk.gov.hmrc.epsmessagerenderer.models.*
import uk.gov.hmrc.epsmessagerenderer.models.nps.NpsPerson
import uk.gov.hmrc.epsmessagerenderer.models.workitem.ProcessingStatus.Succeeded

class HodsAdapterConnectorSpec extends BaseSpec with WireMockHelper with IntegrationPatience {

  override lazy val app: Application = GuiceApplicationBuilder()
    .configure("microservice.services.hods-adapter.port" -> server.port)
    .build()

  def sut: HodsAdapterConnector =
    new HodsAdapterConnector(
      kenshooMetrics,
      http,
      servicesConfig,
      auditing
    ) {
      override def baseUrl: String = s"http://localhost:${server.port}"
    }

  "HodsAdapterConnector" when {

    "setWorkItemStatus is called" must {

      val setWorkItemStatusUrl: String = "/eps-hods-adapter/foo"

      "return true" when {

        "the http call returns NO_CONTENT (204)" in {

          server.stubFor(
            post(setWorkItemStatusUrl)
              .willReturn(
                aResponse.withStatus(NO_CONTENT)
              )
          )

          sut
            .setWorkItemStatus(setWorkItemStatusUrl, NotificationStatus(Succeeded))
            .futureValue mustBe true
        }
      }

      "return false" when {

        "the http call returns any other status" in {

          server.stubFor(
            post(setWorkItemStatusUrl)
              .willReturn(
                aResponse.withStatus(BAD_REQUEST)
              )
          )

          sut
            .setWorkItemStatus(setWorkItemStatusUrl, NotificationStatus(Succeeded))
            .futureValue mustBe false
        }
      }
    }

    "optUserOutOfPrintSuppression is called" must {

      val optUserOutUrl: String =
        s"/eps-hods-adapter/preferences/person/$nino/print-suppression"

      "return true" when {

        "the http call returns OK (200)" in {

          server.stubFor(
            post(optUserOutUrl)
              .willReturn(
                aResponse.withStatus(OK)
              )
          )

          sut
            .optUserOutOfPrintSuppression(nino.value)
            .futureValue mustBe true
        }
      }

      "return false" when {

        "the http call returns any other status" in {

          server.stubFor(
            post(optUserOutUrl)
              .willReturn(
                aResponse.withStatus(BAD_REQUEST)
              )
          )

          sut
            .optUserOutOfPrintSuppression(nino.value)
            .futureValue mustBe false
        }
      }
    }

    "getPerson is called" must {

      val getPersonUrl: String = s"/eps-hods-adapter/preferences/person/$nino"
      val notFoundResponse: String = """{"Error":"NotFound"}"""

      "Return Person if success means person found" in {

        server.stubFor(
          get(getPersonUrl)
            .willReturn(
              aResponse.withBody(Json.toJson(getValidPersonObject(nino.value)).toString())
            )
        )

        val response = sut.getPerson(Nino(nino.value)).futureValue
        val person = response.json.as[NpsPerson]

        response.status mustBe 200
        person.nino mustBe nino.value

      }

      "Return 404 if person is not found" in {

        server.stubFor(
          get(getPersonUrl)
            .willReturn(
              aResponse
                .withStatus(NOT_FOUND)
                .withBody("""{"Error":"NotFound"}""")
            )
        )

        val response = sut.getPerson(Nino(nino.value)).futureValue

        response.status mustBe NOT_FOUND

      }

      "Return Exception if other than 404" in {
        server.stubFor(
          get(getPersonUrl)
            .willReturn(
              aResponse
                .withStatus(SERVICE_UNAVAILABLE)
                .withBody(notFoundResponse)
            )
        )

        val response = await(sut.getPerson(Nino(nino.value)))

        response.status mustBe SERVICE_UNAVAILABLE
      }
    }
  }
}
