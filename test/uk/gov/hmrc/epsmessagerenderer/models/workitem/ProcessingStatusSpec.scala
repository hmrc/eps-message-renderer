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
