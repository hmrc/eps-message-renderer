/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.utils

import play.api.libs.json.{ Format, Reads, Writes }

import java.time.{ Instant, LocalDate, ZoneOffset }
import java.time.format.DateTimeFormatter

//RestFormats equivalent
object DateFormats {

  implicit val instantFormats: Format[Instant] = {
    val dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    val dateTimeWithMillis: DateTimeFormatter =
      DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneOffset.UTC)

    Format(Reads.DefaultInstantReads, Writes.temporalWrites[Instant, DateTimeFormatter](dateTimeWithMillis))
  }

  implicit val localDateFormats: Format[LocalDate] =
    Format(Reads.DefaultLocalDateReads, Writes.DefaultLocalDateWrites)
}
