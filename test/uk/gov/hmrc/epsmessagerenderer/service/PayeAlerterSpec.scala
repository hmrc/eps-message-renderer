/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.service

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{ reset, times, verify, when }
import org.scalatest.concurrent.IntegrationPatience
import play.api.http.Status.{ INTERNAL_SERVER_ERROR, LOCKED, NOT_FOUND, OK }
import play.api.libs.json.Json
import uk.gov.hmrc.domain.{ Nino, TaxIdentifier }
import uk.gov.hmrc.epsmessagerenderer.BaseSpec
import uk.gov.hmrc.epsmessagerenderer.connectors.*
import uk.gov.hmrc.epsmessagerenderer.fixtures.PayeNotificationWorkItemFixture.payeNotificationWorkItem
import uk.gov.hmrc.epsmessagerenderer.models.*
import uk.gov.hmrc.epsmessagerenderer.models.nps.NpsPerson
import uk.gov.hmrc.epsmessagerenderer.models.workitem.ProcessingStatus.Succeeded
import uk.gov.hmrc.epsmessagerenderer.util.LogCapturing
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse }

import scala.concurrent.{ ExecutionContext, Future }

class PayeAlerterSpec extends BaseSpec with IntegrationPatience with LogCapturing {

  val mockPreferencesConnector: PreferencesConnector = mock[PreferencesConnector]
  val mockEmailConnector: EmailConnector = mock[EmailConnector]
  val mockMobileConnector: MobileConnector = mock[MobileConnector]

  override protected def beforeEach(): Unit = {
    reset(mockPreferencesConnector)
    reset(mockEmailConnector)
    reset(mockMobileConnector)
    reset(mockMetrics)
  }

  trait LocalSetup {

    def getPersonResponse: Future[HttpResponse] = Future.successful {
      val body = Json.toJson(NpsPerson(nino.value, "Mr", "John", "Doe"))
      HttpResponse.apply(
        OK,
        body.toString()
      )

    }

    def setWorkItemStatusResponse: Future[Boolean] = Future.successful(true)

    object StubHodsAdapterConnector
        extends HodsAdapterConnector(
          kenshooMetrics,
          http,
          servicesConfig,
          auditing
        ) {
      override def optUserOutOfPrintSuppression(
        nino: Metric
      )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = Future.successful(true)

      override def getPerson(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
        getPersonResponse

      override def setWorkItemStatus(path: String, processingStatus: NotificationStatus)(implicit
        hc: HeaderCarrier,
        ec: ExecutionContext
      ): Future[Boolean] =
        setWorkItemStatusResponse
    }

    def sut: PayeAlerter = new PayeAlerter(
      StubHodsAdapterConnector,
      mockPreferencesConnector,
      mockEmailConnector,
      mockMobileConnector,
      mockMetrics,
      auditing
    )

    val payeAlert: PayeNotificationWorkItem = payeNotificationWorkItem(nino.value)

    def getVerifiedEmailAddressResponse: Future[VerifiedEmailAddressResponse] =
      Future.successful(EmailValidation("test@gmail.com"))

    when(
      mockPreferencesConnector.getVerifiedEmailAddress(any[TaxIdentifier])(any[HeaderCarrier], any[ExecutionContext])
    ) thenReturn getVerifiedEmailAddressResponse

    when(
      mockEmailConnector
        .sendPayeAlert(any[String], any[String], any[Nino])(any[HeaderCarrier])
    ) thenReturn (Future.successful(()))

    when(
      mockMobileConnector.checkAndSendNotification(any[Nino])(any[HeaderCarrier], any[ExecutionContext])
    ) thenReturn Future.successful(())

  }

  "PayeAlerter" when {

    "processNotification is called" must {

      "send paye alert" when {

        "an email address and person are found" in new LocalSetup {

          sut.processNotification(payeAlert).futureValue mustBe true

          verify(mockEmailConnector).sendPayeAlert(any[String], any[String], any[Nino])(
            any[HeaderCarrier]
          )
        }
      }

      "not send paye alert & successfully set the workItem status" when {

        "getPerson returns LOCKED (423)" in new LocalSetup {

          override def getPersonResponse: Future[HttpResponse] =
            Future.successful(HttpResponse.apply(LOCKED, ""))

          sut.processNotification(payeAlert).futureValue

          verify(mockEmailConnector, times(0)).sendPayeAlert(any[String], any[String], any[Nino])(
            any[HeaderCarrier]
          )
        }

        "getPerson returns any other status code" in new LocalSetup {

          override def getPersonResponse: Future[HttpResponse] =
            Future.successful(HttpResponse.apply(INTERNAL_SERVER_ERROR, ""))

          sut.processNotification(payeAlert).futureValue

          verify(mockEmailConnector, times(0))
            .sendPayeAlert(any[String], any[String], any[Nino])(any[HeaderCarrier])
        }

        "getVerifiedEmailAddress returns VerifiedEmailNotFound" in new LocalSetup {

          override def getVerifiedEmailAddressResponse: Future[VerifiedEmailAddressResponse] =
            Future.successful(VerifiedEmailNotFound("DE_ENROLLING"))

          sut.processNotification(payeAlert).futureValue

          verify(mockEmailConnector, times(0))
            .sendPayeAlert(any[String], any[String], any[Nino])(any[HeaderCarrier])
        }

        "getVerifiedEmailAddress returns VerifiedEmailError" in new LocalSetup {

          override def getVerifiedEmailAddressResponse: Future[VerifiedEmailAddressResponse] =
            Future.successful(VerifiedEmailError("Error reason"))

          sut.processNotification(payeAlert).futureValue

          verify(mockEmailConnector, times(0))
            .sendPayeAlert(any[String], any[String], any[Nino])(any[HeaderCarrier])
        }

        "getVerifiedEmailAddress returns an exception" in new LocalSetup {
          override def getVerifiedEmailAddressResponse: Future[VerifiedEmailAddressResponse] =
            Future.failed(new OtherException("Could not find email"))

          sut.processNotification(payeAlert).futureValue

          verify(mockEmailConnector, times(0))
            .sendPayeAlert(any[String], any[String], any[Nino])(any[HeaderCarrier])
        }

        "getPerson returns NOT_FOUND (404)" in new LocalSetup {

          override def getPersonResponse: Future[HttpResponse] =
            Future.successful(HttpResponse.apply(NOT_FOUND, ""))

          sut.processNotification(payeAlert).futureValue

          verify(mockEmailConnector, times(0))
            .sendPayeAlert(any[String], any[String], any[Nino])(any[HeaderCarrier])
        }

        "getPerson returns an exception" in new LocalSetup {

          override def getPersonResponse: Future[HttpResponse] =
            Future.failed(new Exception("Something went wrong"))

          sut.processNotification(payeAlert).futureValue

          verify(mockEmailConnector, times(0))
            .sendPayeAlert(any[String], any[String], any[Nino])(any[HeaderCarrier])
        }
      }
    }

    "setStatus is called" must {

      "return true" when {

        "setWorkItemStatus returns true" in new LocalSetup {

          sut.setStatus(Succeeded, "foo").futureValue mustBe true
        }
      }

      "return false" when {

        "setWorkItemStatus returns false" in new LocalSetup {
          override def setWorkItemStatusResponse: Future[Boolean] =
            Future.successful(false)

          sut.setStatus(Succeeded, "foo").futureValue mustBe false
        }
      }
    }
  }
}
