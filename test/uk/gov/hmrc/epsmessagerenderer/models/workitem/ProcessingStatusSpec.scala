/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.models.workitem

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{ JsError, JsString, Json }
import uk.gov.hmrc.epsmessagerenderer.models.workitem.ProcessingStatus.{ InProgress, PermanentlyFailed, ToDo }
import uk.gov.hmrc.play.audit.model.EventTypes.Succeeded

class ProcessingStatusSpec extends PlaySpec {

  "ProcessingStatus" must {

    "successfully read all valid statuses" in {
      ProcessingStatus.values.foreach { status =>
        JsString(status.name).as[ProcessingStatus](ProcessingStatus.format) mustBe status
      }
    }

    "fail to read an unknown status string" in {
      val invalidStatus = "not-a-status"
      val result = JsString(invalidStatus).validate[ProcessingStatus](ProcessingStatus.format)

      result mustBe JsError(s"Could not convert to ProcessingStatus from $invalidStatus")
    }

    "write to the correct string value" in {
      Json.toJson(ToDo)(ProcessingStatus.format) mustBe JsString("todo")
      Json.toJson(InProgress)(ProcessingStatus.format) mustBe JsString("in-progress")
      Json.toJson(PermanentlyFailed)(ProcessingStatus.format) mustBe JsString("permanently-failed")
    }

    "correctly identify cancellable statuses" in {
      ProcessingStatus.cancellable must contain(ToDo)
      ProcessingStatus.cancellable mustNot contain(InProgress)
      ProcessingStatus.cancellable mustNot contain(Succeeded)
    }
  }
}
