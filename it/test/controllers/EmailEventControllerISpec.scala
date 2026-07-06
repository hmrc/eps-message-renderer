/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package controllers

import com.github.tomakehurst.wiremock.client.WireMock.{ aResponse, post, urlEqualTo }
import play.api.http.Status.NO_CONTENT
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.{ FakeHeaders, FakeRequest }
import uk.gov.hmrc.epsmessagerenderer.models.nps.NpsPrintSuppressionUpdateRequest
import utils.IntegrationSpec

class EmailEventControllerISpec extends IntegrationSpec {

  override def fakeApplication() =
    GuiceApplicationBuilder()
      .configure(
        "microservice.services.auth.port"            -> server.port(),
        "microservice.services.citizen-details.port" -> server.port(),
        "microservice.services.hods-adapter.host"    -> "127.0.0.1",
        "microservice.services.hods-adapter.port"    -> server.port()
      )
      .build()

  "/eps-message-renderer/process-event" must {

    val processEventUrl = s"/eps-message-renderer/process-event/$generatedNino"
    val printSuppressionUrl =
      s"/eps-hods-adapter/preferences/person/$generatedNino/print-suppression"

    "return an OK response" in {
      val processEventsBody = FakeRequest(
        method = "POST",
        uri = processEventUrl,
        headers = FakeHeaders(),
        body = Json.parse(
          """
            |{
            |  "events": [
            |    { "event" : "delivered",       "detected" : "2015-07-09T09:37:44.989Z" },
            |    { "event" : "opened",          "detected" : "2015-07-09T09:37:43.989Z" },
            |    { "event" : "sent",            "detected" : "2015-07-09T09:37:42.989Z" },
            |    { "event" : "PermanentBounce", "detected" : "2015-07-09T09:37:45.989Z" },
            |    { "event" : "PermanentBounce", "detected" : "2015-07-09T09:37:45.989Z" }
            |  ]
            |}""".stripMargin
        )
      )

      server.stubFor(
        post(urlEqualTo(printSuppressionUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(NpsPrintSuppressionUpdateRequest("formType", "outputPreference", false).toString)
          )
      )
      val result = route(app, processEventsBody)
      result.map(status) mustBe Some(OK)
    }

    List(BAD_REQUEST, NOT_FOUND, IM_A_TEAPOT, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE, BAD_GATEWAY).foreach {
      httpStatus =>
        s"return a NO_CONTENT response for $httpStatus response $httpStatus" in {
          val processEventsBody = FakeRequest(
            method = "POST",
            uri = processEventUrl,
            headers = FakeHeaders(),
            body = Json.parse(
              """
                |{
                |  "events": [
                |    { "event" : "delivered",       "detected" : "2015-07-09T09:37:44.989Z" },
                |    { "event" : "opened",          "detected" : "2015-07-09T09:37:43.989Z" },
                |    { "event" : "sent",            "detected" : "2015-07-09T09:37:42.989Z" },
                |    { "event" : "PermanentBounce", "detected" : "2015-07-09T09:37:45.989Z" },
                |    { "event" : "PermanentBounce", "detected" : "2015-07-09T09:37:45.989Z" }
                |  ]
                |}""".stripMargin
            )
          )
          server.stubFor(
            post(urlEqualTo(printSuppressionUrl))
              .willReturn(aResponse().withStatus(httpStatus))
          )
          val result = route(app, processEventsBody)
          result.map(status) mustBe Some(NO_CONTENT)
        }
    }
  }
}
