/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.controllers

import org.mockito.ArgumentMatchers.{ any, eq as eqTo }
import org.mockito.Mockito.{ reset, times, verify, when }
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.{ contentAsString, defaultAwaitTimeout, status }
import play.api.test.{ FakeHeaders, FakeRequest }
import uk.gov.hmrc.epsmessagerenderer.models.EmailEvents
import uk.gov.hmrc.epsmessagerenderer.service.EmailEventService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ ExecutionContext, Future }

class EmailEventControllerSpec extends PlaySpec with BeforeAndAfterEach with ScalaFutures with GuiceOneAppPerSuite {

  val mockEmailEventService: EmailEventService = mock[EmailEventService]

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .overrides(
        bind[EmailEventService].toInstance(mockEmailEventService)
      )
      .configure(
        "metrics.enabled" -> "false"
      )
      .build()

  val controller = fakeApplication().injector.instanceOf[EmailEventController]

  override protected def beforeEach(): Unit = reset(
    mockEmailEventService
  )

  "process events for nino" must {
    "opt out user if there is a PermanentBounce in the events" in new TestCase {
      val processEventsBody = FakeRequest(
        method = "POST",
        uri = "/eps-message-renderer/process-event/" + nino,
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

      val expectedEvents = processEventsBody.body.as[EmailEvents]
      when(
        mockEmailEventService
          .processEventsFor(eqTo(expectedEvents), eqTo(nino))(any[HeaderCarrier], any[ExecutionContext])
      )
        .thenReturn(Future.successful(true))

      val result = controller.processEvent(nino)(processEventsBody)
      status(result) mustBe 200
      contentAsString(result) mustBe "User opted out of print suppression"
      verify(mockEmailEventService, times(1))
        .processEventsFor(eqTo(expectedEvents), eqTo(nino))(any[HeaderCarrier], any[ExecutionContext])
    }

    "take no action if there is no  PermanentBounce in the events" in new TestCase {
      val processEventsBody = FakeRequest(
        method = "POST",
        uri = "/eps-message-renderer/process-event/" + nino,
        headers = FakeHeaders(),
        body = Json.parse(
          """
            |{
            |  "events": [
            |    { "event" : "delivered",       "detected" : "2015-07-09T09:37:44.989Z" },
            |    { "event" : "opened",          "detected" : "2015-07-09T09:37:43.989Z" },
            |    { "event" : "sent",            "detected" : "2015-07-09T09:37:42.989Z" }
            |  ]
            |}""".stripMargin
        )
      )

      val expectedEvents = processEventsBody.body.as[EmailEvents]

      when(
        mockEmailEventService
          .processEventsFor(eqTo(expectedEvents), eqTo(nino))(any[HeaderCarrier], any[ExecutionContext])
      )
        .thenReturn(Future.successful(false))

      val result = controller.processEvent(nino)(processEventsBody)

      status(result) mustBe 204
      verify(mockEmailEventService, times(1))
        .processEventsFor(eqTo(expectedEvents), eqTo(nino))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  trait TestCase {
    val nino = "AB123456C"
  }

}
