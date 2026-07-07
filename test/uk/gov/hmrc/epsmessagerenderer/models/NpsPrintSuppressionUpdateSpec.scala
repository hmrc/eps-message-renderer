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

package uk.gov.hmrc.epsmessagerenderer.models

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import uk.gov.hmrc.epsmessagerenderer.models.nps.NpsPrintSuppressionUpdateRequest.{ OutputPreference, PayeFormType }
import uk.gov.hmrc.epsmessagerenderer.models.nps.{ NpsPrintSuppressionUpdateRequest, NpsPrintSuppressionUpdateResponse }

/** Created by AdamT on 14/08/15.
  */
class NpsPrintSuppressionUpdateSpec extends PlaySpec {

  "NpsPrintSuppressionUpdateRequest" must {

    "write a json object to an object of itself" in {

      val printSuppressionUpdateRequest =
        NpsPrintSuppressionUpdateRequest(PayeFormType.p2, OutputPreference.digital, false)
      val jsonUpdateRequest = Json.obj("formType" -> "P2", "outputPreference" -> "digital", "bounced" -> false)

      Json.toJson(printSuppressionUpdateRequest) mustBe jsonUpdateRequest
    }

    "read a json object to an object of itself" in {
      val jsonUpdateRequest = Json.obj("formType" -> "P2", "outputPreference" -> "digital", "bounced" -> false)

      val result = jsonUpdateRequest.as[NpsPrintSuppressionUpdateRequest]

      result.formType mustBe "P2"
      result.outputPreference mustBe "digital"
      result.bounced mustBe false
    }

  }

  "NpsPrintSuppressionUpdateResponse" must {

    "write a json object to an object of itself" in {

      val printSuppressionUpdateResponse = NpsPrintSuppressionUpdateResponse(0)
      val jsonUpdateResponse = Json.obj("rejectionCode" -> 0)

      Json.toJson(printSuppressionUpdateResponse) mustBe jsonUpdateResponse
    }

    "read a json object to an object of itself" in {
      val jsonUpdateResponse = Json.obj("rejectionCode" -> 0)

      val result = jsonUpdateResponse.as[NpsPrintSuppressionUpdateResponse]

      result.rejectionCode mustBe 0
    }

  }

}
