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

package uk.gov.hmrc.epsmessagerenderer.models.nps

import play.api.libs.json.{ Json, OFormat }

case class NpsPrintSuppressionUpdateRequest(formType: String, outputPreference: String, bounced: Boolean) {

  def getAuditingDetails: Map[String, String] = Map(
    "formType"         -> formType,
    "outputPreference" -> outputPreference,
    "bounced"          -> bounced.toString
  )
}

object NpsPrintSuppressionUpdateRequest {

  implicit val formats: OFormat[NpsPrintSuppressionUpdateRequest] =
    Json.format[NpsPrintSuppressionUpdateRequest]

  object PayeFormType {
    val p2 = "P2"
  }

  object OutputPreference {
    val paper = "paper"
    val digital = "digital"
  }

}

case class NpsPrintSuppressionUpdateResponse(rejectionCode: Int)

object NpsPrintSuppressionUpdateResponse {
  implicit val formats: OFormat[NpsPrintSuppressionUpdateResponse] =
    Json.format[NpsPrintSuppressionUpdateResponse]
}
