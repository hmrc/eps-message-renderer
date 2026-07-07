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

import play.api.libs.json.{ Json, Writes }

final case class EmailAlert(
  to: List[String],
  templateId: String,
  parameters: Map[String, String],
  force: Option[Boolean] = None,
  eventUrl: Option[String] = None,
  tags: Map[String, String] = Map.empty[String, String]
)

object EmailAlert {
  implicit val sendTemplatedEmailRequestWrites: Writes[EmailAlert] =
    Json.format[EmailAlert]
}
