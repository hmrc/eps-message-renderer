/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.models

import play.api.libs.json.{ Json, OFormat }

final case class EmailValidation(email: String) extends VerifiedEmailAddressResponse

object EmailValidation {
  implicit val formats: OFormat[EmailValidation] = Json.format[EmailValidation]
}

sealed trait VerifiedEmailAddressResponse

final case class VerifiedEmailNotFound(reasonCode: String) extends VerifiedEmailAddressResponse {

  def getMessage: String =
    reasonCode match {
      case "DE_ENROLLING"               => "User De-Enrolment Received"
      case "EMAIL_ADDRESS_NOT_VERIFIED" => "email not verified or bounced"
      case "NOT_OPTED_IN"               => "email not verified as user not opted in"
      case "PREFERENCES_NOT_FOUND" =>
        "email not verified as preferences not found"
      case _ => "email not verified for unknown reason"
    }

}

final case class VerifiedEmailError(reason: String) extends VerifiedEmailAddressResponse
