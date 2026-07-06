/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.connectors

import com.github.tomakehurst.wiremock.client.WireMock.{ aResponse, get }
import com.github.tomakehurst.wiremock.http.Fault
import org.scalatest.concurrent.IntegrationPatience
import play.api.Application
import play.api.http.Status.{ INTERNAL_SERVER_ERROR, NOT_FOUND, OK }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.epsmessagerenderer.BaseSpec
import uk.gov.hmrc.epsmessagerenderer.models.{ EmailValidation, VerifiedEmailNotFound }

class PreferencesConnectorSpec extends BaseSpec with WireMockHelper with IntegrationPatience {

  override lazy val app: Application = GuiceApplicationBuilder()
    .configure("microservice.services.preferences.port" -> server.port)
    .build()

  def sut: PreferencesConnector =
    new PreferencesConnector(kenshooMetrics, http, servicesConfig, auditing) {
      override def baseUrl: String = s"http://localhost:${server.port}"
    }

  "PreferencesConnector" when {

    "getVerifiedEmailAddress is called" must {

      val getEmailUrl = s"/preferences/verified-email?regime=paye&taxId=$nino"

      "return an EmailValidation object" when {
        "status code is 200" in {

          val emailValidation = EmailValidation("testemail@email.com")

          server.stubFor(
            get(getEmailUrl)
              .willReturn(
                aResponse
                  .withStatus(OK)
                  .withBody(Json.toJson(emailValidation).toString())
              )
          )

          sut.getVerifiedEmailAddress(nino).futureValue mustBe emailValidation
        }
      }

      "return VerifiedEmailNotFound object" when {
        "status code is 404" in {

          val reason = "EMAIL_ADDRESS_NOT_VERIFIED"

          server.stubFor(
            get(getEmailUrl)
              .willReturn(
                aResponse
                  .withStatus(NOT_FOUND)
                  .withBody(reason)
              )
          )

          sut
            .getVerifiedEmailAddress(nino)
            .futureValue mustBe VerifiedEmailNotFound(reason)
        }
      }

      "thrown an exception" when {
        "status code is anything else" in {

          server.stubFor(
            get(getEmailUrl)
              .willReturn(
                aResponse.withStatus(INTERNAL_SERVER_ERROR)
              )
          )

          the[Exception] thrownBy {
            sut.getVerifiedEmailAddress(nino).futureValue
          } must have message
            s"The future returned an exception of type: java.lang.Exception, with message: OTHER_EXCEPTION_$INTERNAL_SERVER_ERROR."
        }

        "when the call to the connector fails" in {

          server.stubFor(
            get(getEmailUrl)
              .willReturn(
                aResponse.withFault(Fault.EMPTY_RESPONSE)
              )
          )

          val executed = sut.getVerifiedEmailAddress(nino)

          an[Exception] must be thrownBy executed.futureValue
        }
      }
    }
  }
}
