/*
 * Copyright 2023 HM Revenue & Customs
 *
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
