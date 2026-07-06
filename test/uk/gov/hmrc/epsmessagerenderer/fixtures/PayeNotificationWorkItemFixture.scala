/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.fixtures

import play.api.libs.json.{ JsValue, Json }
import uk.gov.hmrc.epsmessagerenderer.models.PayeNotificationWorkItem
import uk.gov.hmrc.epsmessagerenderer.utils.DateFormats

import java.time.Instant

object PayeNotificationWorkItemFixture {

  val dateTime = DateFormats.instantFormats.writes(Instant.now()).toString()

  def rawJson(nino: String): JsValue = Json.parse(
    s"""{
       |    "id":"55c33436cb04001d05de9689",
       |    "modifiedDetails": {
       |        "createdAt": $dateTime,
       |        "lastUpdated": $dateTime
       |    },
       |    "availableAt": $dateTime,
       |    "status": "todo",
       |    "failures": 0,
       |    "alerts": {
       |        "alert" : {
       |            "identifier": {
       |                "id_type": "nino",
       |                "value": "$nino"
       |            },
       |            "hod_id": "nps",
       |            "template_id": "P2"
       |        }
       |    },
       |    "statusUrl": "/preferences/alert/print-suppression/55c33418cb04001c05de9585/status"
       |}""".stripMargin
  )

  def payeNotificationWorkItem(nino: String): PayeNotificationWorkItem =
    rawJson(nino).as[PayeNotificationWorkItem]

}
