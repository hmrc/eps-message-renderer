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

package uk.gov.hmrc.epsmessagerenderer

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.epsmessagerenderer.models.PayePrintSuppressionNotification

package object service {

  private val TAX_ESTIMATE_MESSAGE_ALERT_ID = "tax_estimate_message_alert"
  private val DAILY_TAX_ESTIMATE_MESSAGE_ALERT_ID = "daily_tax_estimate_message_alert"
  private val ANNUAL_TAX_ESTIMATE_MESSAGE_ALERT_ID = "annual_tax_estimate_message_alert"

  def ninoFromInputStringOrAppendTempSuffix(originalNino: String): Nino = {
    val ninoLengthWithoutSuffix = 8
    val originalNinoAfterTrim = originalNino.trim

    if originalNinoAfterTrim.length > ninoLengthWithoutSuffix then Nino(originalNinoAfterTrim)
    else Nino(originalNinoAfterTrim + "A")
  }

  def templateIdForEmailAlert(payePrintSupNotif: PayePrintSuppressionNotification): String =
    payePrintSupNotif.notice_type.fold(TAX_ESTIMATE_MESSAGE_ALERT_ID) { noticeType =>
      noticeType.trim.toLowerCase match {
        case "cy" => DAILY_TAX_ESTIMATE_MESSAGE_ALERT_ID
        case _    => ANNUAL_TAX_ESTIMATE_MESSAGE_ALERT_ID
      }
    }
}
