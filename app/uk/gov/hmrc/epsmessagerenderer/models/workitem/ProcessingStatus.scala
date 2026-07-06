/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.models.workitem

import play.api.libs.json.{ Format, JsError, JsString, JsSuccess, JsValue, Reads, Writes }

sealed trait ProcessingStatus {
  def name: String
}

sealed trait ResultStatus extends ProcessingStatus

object ProcessingStatus {
  outer =>

  case object ToDo extends ProcessingStatus { override val name = "todo" }
  case object InProgress extends ProcessingStatus {
    override val name = "in-progress"
  }
  case object Succeeded extends ResultStatus { override val name = "succeeded" }
  case object Deferred extends ResultStatus { override val name = "deferred" }
  case object Failed extends ResultStatus { override val name = "failed" }
  case object PermanentlyFailed extends ResultStatus {
    override val name = "permanently-failed"
  }
  case object Ignored extends ResultStatus { override val name = "ignored" }
  case object Duplicate extends ResultStatus { override val name = "duplicate" }
  case object Cancelled extends ResultStatus { override val name = "cancelled" }

  val values: Set[ProcessingStatus] =
    Set(
      ToDo,
      InProgress,
      Succeeded,
      Failed,
      PermanentlyFailed,
      Ignored,
      Duplicate,
      Deferred,
      Cancelled
    )

  val cancellable: Set[ProcessingStatus] =
    Set(
      ToDo,
      Failed,
      PermanentlyFailed,
      Ignored,
      Duplicate,
      Deferred
    )

  private val nameToStatus: Map[String, ProcessingStatus] =
    values.map(s => (s.name, s)).toMap

  val reads: Reads[ProcessingStatus] =
    Reads[ProcessingStatus] { json =>
      json
        .validate[String]
        .flatMap(n =>
          nameToStatus.get(n) match {
            case Some(s) => JsSuccess(s)
            case None =>
              JsError(s"Could not convert to ProcessingStatus from $n")
          }
        )
    }

  val writes: Writes[ProcessingStatus] =
    new Writes[ProcessingStatus] {
      override def writes(status: ProcessingStatus): JsValue =
        JsString(status.name)
    }

  val format: Format[ProcessingStatus] =
    Format(reads, writes)
}
