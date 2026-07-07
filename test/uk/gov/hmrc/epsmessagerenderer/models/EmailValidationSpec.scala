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
