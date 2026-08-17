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

package uk.gov.hmrc.epsmessagerenderer.service

import uk.gov.hmrc.epsmessagerenderer.BaseSpec
import uk.gov.hmrc.domain.Nino

class PackageSpec extends BaseSpec {

  "ninoFromInputStringOrAppendTempSuffix" should {

    "add temporary suffix A" when {
      "original Nino is of length 8" in {
        val extraSpace = "  "

        val inputNinoString1 = "AT657550"
        val inputNinoString2 = "SN659950"
        val inputNinoString3 = s"SN659950$extraSpace"

        val result1: Nino = ninoFromInputStringOrAppendTempSuffix(inputNinoString1)
        result1 mustBe a[Nino]
        result1.value mustBe s"${inputNinoString1}A"

        val result2: Nino = ninoFromInputStringOrAppendTempSuffix(inputNinoString2)
        result2 mustBe a[Nino]
        result2.value mustBe s"${inputNinoString2}A"

        val result3: Nino = ninoFromInputStringOrAppendTempSuffix(inputNinoString3)
        result3 mustBe a[Nino]
        result3.value mustBe s"${inputNinoString3.trim}A"
      }
    }

    "create a Nino with original string value" when {
      "original Nino is of length greater than 8" in {

        val inputNinoString = "AT657550C"
        val result: Nino = ninoFromInputStringOrAppendTempSuffix(inputNinoString)

        result mustBe a[Nino]
        result.nino mustBe inputNinoString
      }
    }
  }
}
