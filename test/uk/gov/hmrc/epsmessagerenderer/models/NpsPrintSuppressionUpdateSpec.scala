/*
 * Copyright 2023 HM Revenue & Customs
 *
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
