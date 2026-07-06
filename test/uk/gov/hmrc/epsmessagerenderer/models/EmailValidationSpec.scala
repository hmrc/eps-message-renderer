/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.models

import org.scalatestplus.play.PlaySpec

class EmailValidationSpec extends PlaySpec {

  "VerifiedEmailNotFound" must {

    "return de-enrolment message for de-enrolment reason" in {
      VerifiedEmailNotFound("DE_ENROLLING").getMessage mustBe "User De-Enrolment Received"
    }

    "return email not verified message for email not verified reason" in {
      VerifiedEmailNotFound("EMAIL_ADDRESS_NOT_VERIFIED").getMessage mustBe "email not verified or bounced"
    }

    "return not opted in message for utr not opted in reason" in {
      VerifiedEmailNotFound("NOT_OPTED_IN").getMessage mustBe "email not verified as user not opted in"
    }

    "return preferences not found for not found reason" in {
      VerifiedEmailNotFound("PREFERENCES_NOT_FOUND").getMessage mustBe "email not verified as preferences not found"
    }

    "return generic error message when reason code is other exception" in {
      VerifiedEmailNotFound("OTHER_EXCEPTION").getMessage mustBe "email not verified for unknown reason"
    }

    "return generic error message when reason code does not match a valid reason" in {
      VerifiedEmailNotFound("not a valid reason").getMessage mustBe "email not verified for unknown reason"
    }
  }
}
