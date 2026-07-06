/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.service

import java.time.Instant
import org.mockito.ArgumentMatchers.{ any, eq => eqTo }
import org.mockito.Mockito.{ reset, times, verify, when }
import org.scalatest.concurrent.IntegrationPatience
import uk.gov.hmrc.epsmessagerenderer.BaseSpec
import uk.gov.hmrc.epsmessagerenderer.connectors._
import uk.gov.hmrc.epsmessagerenderer.models._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ ExecutionContext, Future }

class EmailEventServiceSpec extends BaseSpec with IntegrationPatience {

  val mockHodsAdapterConnector: HodsAdapterConnector =
    mock[HodsAdapterConnector]

  override protected def beforeEach(): Unit = reset(
    mockHodsAdapterConnector
  )

  trait LocalSetup {

    def sut: EmailEventService = new EmailEventService(
      mockHodsAdapterConnector
    )

    def getVerifiedEmailAddressResponse: Future[VerifiedEmailAddressResponse] =
      Future.successful(EmailValidation("test@gmail.com"))

    when(
      mockHodsAdapterConnector.optUserOutOfPrintSuppression(any[String])(any[HeaderCarrier], any[ExecutionContext])
    ) thenReturn Future.successful(true)

    val sampleDate: Instant = Instant.ofEpochMilli(1436434662989L)

    val nino = "AB123456C"

  }

  "EmailEventService" when {

    "processEventsFor is called" must {

      "call hodsAdapterConnector to opt out of print suppression " when {

        "event list contains PermanentBounce event" in new LocalSetup {
          val sampleEvents = EmailEvents(
            List(
              EmailEvent(Other, sampleDate),
              EmailEvent(PermanentBounce, sampleDate)
            )
          )
          sut.processEventsFor(sampleEvents, nino).futureValue mustBe true
          verify(mockHodsAdapterConnector, times(1))
            .optUserOutOfPrintSuppression(eqTo(nino))(any[HeaderCarrier], any[ExecutionContext])
        }
      }
      "do not call hodsAdapterConnector to opt out of print suppression " when {

        "event list does not contains PermanentBounce event" in new LocalSetup {
          val sampleEvents = EmailEvents(
            List(
              EmailEvent(Other, sampleDate)
            )
          )
          sut.processEventsFor(sampleEvents, nino).futureValue mustBe false
          verify(mockHodsAdapterConnector, times(0))
            .optUserOutOfPrintSuppression(any[String])(any[HeaderCarrier], any[ExecutionContext])
        }
      }

    }
  }
}
