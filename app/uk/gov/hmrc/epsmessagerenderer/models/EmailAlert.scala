/*
 * Copyright 2023 HM Revenue & Customs
 *
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
