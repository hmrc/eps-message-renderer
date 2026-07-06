/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package utils

import org.mockito.ArgumentMatchers.{ any, argThat }
import org.mockito.Mockito.{ reset, verify, verifyNoInteractions }
import org.scalatest.concurrent.Eventually
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.epsmessagerenderer.models.MobileNotificationAudit
import uk.gov.hmrc.epsmessagerenderer.utils.Auditing
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.{ DataEvent, EventTypes }

import scala.concurrent.ExecutionContext.Implicits.global

class AuditingSpec extends PlaySpec with MockitoSugar with Eventually {

  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val auditing = new Auditing(mockAuditConnector)

  "CreateAudit" must {
    "send a dataEvent for MobileNotificationAudit with correct details " in new TestCase {
      auditing.createAudit(auditType, message, auditItem)

      verify(mockAuditConnector).sendEvent(argThat { (event: DataEvent) =>
        event.auditSource mustBe "eps-message-renderer"
        event.auditType mustBe auditType
        event.tags mustBe Map("transactionName" -> message)
        event.detail mustBe Map(
          "identifier" -> "nino",
          "value"      -> nino.value,
          "status"     -> "200",
          "error"      -> "No Error"
        )
        true
      })(any(), any())
    }

    "log info when an invalid status is passed and not send an event" in new TestCase {
      auditing.createAudit("INVALID_TYPE", "some message", auditItem)
      verifyNoInteractions(mockAuditConnector)
    }

    "throw IllegalArgumentException when an unsupported object type is passed" in new TestCase {
      intercept[IllegalArgumentException] {
        auditing.createAudit(auditType, "some message", unsupportedItem)
      }.getMessage must include("is not supported for auditing")
    }

  }

  class TestCase {
    reset(mockAuditConnector)
    val nino = Nino("AA123456A")
    val auditItem = MobileNotificationAudit(nino, 200, Some("No Error"))
    val auditType = EventTypes.Succeeded
    val message = "Mobile Auth Success"
    val unsupportedItem = "This is a string"
  }

}
