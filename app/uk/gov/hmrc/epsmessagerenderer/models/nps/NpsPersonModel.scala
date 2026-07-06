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

package uk.gov.hmrc.epsmessagerenderer.models.nps

import play.api.libs.json.{ JsValue, Json, OFormat, Writes }

case class PersonResult(status: Int, person: Option[NpsPerson])
case class NpsPerson(nino: String, title: String, firstName: String, lastName: String)

object NpsPerson {

  implicit val npsPersonAlertsFormat: OFormat[NpsPerson] =
    Json.format[NpsPerson]
  implicit val npsPersonAlertsFormatUpdate: Writes[NpsPerson] =
    new Writes[NpsPerson] {
      def writes(npsPerson: NpsPerson): JsValue =
        Json.toJson(npsPerson)
    }

  def getTaxpayersName(person: NpsPerson): Map[String, String] =
    Map(
      "title"    -> person.title.toLowerCase.capitalize,
      "forename" -> person.firstName.toLowerCase.capitalize,
      "surname"  -> person.lastName.toLowerCase.capitalize
    )

}

object SalutationHelper {

  case class Salutation(
    title: Option[Any],
    forename: Option[Any],
    secondForename: Option[Any],
    surname: Option[Any],
    hons: Option[Any]
  )

  def salutationFrom(params: Map[String, Any]): String = {
    val salutation = Salutation(
      params.get("title"),
      params.get("forename"),
      params.get("secondForename"),
      params.get("surname"),
      params.get("honours")
    )

    val salutationString = salutation match {
      case Salutation(None, Some(forename), None, None, None) => s"$forename"
      case Salutation(None, Some(forename), None, Some(surname), None) =>
        s"$forename $surname"
      case Salutation(Some(title), None, None, Some(surname), None) =>
        s"$title $surname"
      case Salutation(Some(title), Some(forename), None, Some(surname), None) =>
        s"$title $forename $surname"
      case Salutation(None, Some(forename), None, Some(surname), Some(honours)) =>
        s"$forename $surname $honours"
      case Salutation(None, Some(forename), Some(secondForename), Some(surname), Some(honours)) =>
        s"$forename $secondForename $surname $honours"
      case Salutation(Some(title), None, None, Some(surname), Some(honours)) =>
        s"$title $surname $honours"
      case Salutation(Some(title), Some(forename), None, Some(surname), Some(honours)) =>
        s"$title $forename $surname $honours"
      case Salutation(Some(title), Some(forename), Some(secondForename), Some(surname), None) =>
        s"$title $forename $secondForename $surname"
      case Salutation(Some(title), Some(forename), Some(secondForename), Some(surname), Some(honours)) =>
        s"$title $forename $secondForename $surname $honours"
      case _ => "Hi"
    }

    s"$salutationString"
  }
}
