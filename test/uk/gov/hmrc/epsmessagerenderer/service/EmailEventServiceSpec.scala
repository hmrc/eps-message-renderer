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
