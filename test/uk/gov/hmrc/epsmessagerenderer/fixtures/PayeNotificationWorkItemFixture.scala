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
