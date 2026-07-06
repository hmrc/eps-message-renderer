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
import play.api.libs.json.Json
import uk.gov.hmrc.epsmessagerenderer.models.nps.{ NpsPerson, SalutationHelper }

class NpsPersonModelSpec extends PlaySpec {

  "NpsPerson" must {

    "convert a json object of the correct format into an NpsPerson" in {

      val person = NpsPerson(nino = "AB123456", title = "Mr", firstName = "Joe", lastName = "Bloggs")
      val jsonPerson = Json.parse(
        """{
          |    "etag" :"foo",
          |    "nino":"AB123456",
          |    "title": "Mr",
          |    "firstName": "Joe",
          |    "lastName": "Bloggs",
          |    "deceased":false
          |}""".stripMargin
      )

      jsonPerson.as[NpsPerson] mustBe person

    }

    "write a json object to an object of itself" in {

      val person = NpsPerson(nino = "AB123456", title = "Mr", firstName = "Joe", lastName = "Bloggs")
      val jsonPerson = Json.parse(
        """{
          |    "nino":"AB123456",
          |    "title": "Mr",
          |    "firstName": "Joe",
          |    "lastName": "Bloggs"
          |}""".stripMargin
      )

      NpsPerson.npsPersonAlertsFormatUpdate.writes(person) mustBe jsonPerson

    }

    "get the taxpayers name from an NpsPerson object and return this as a Map[String, String]" in {
      val person = NpsPerson(nino = "AB123456", title = "Mr", firstName = "Joe", lastName = "Bloggs")
      val taxpayersName = Map(
        "title"    -> "Mr",
        "forename" -> "Joe",
        "surname"  -> "Bloggs"
      )
      NpsPerson.getTaxpayersName(person) mustBe taxpayersName
    }

  }

  "SalutationHelper" must {

    "render a salutation with a forename" in {
      val taxpayersName = Map("forename" -> "Joe")
      SalutationHelper.salutationFrom(taxpayersName) mustBe "Joe"
    }

    "render a salutation with a forename and surname" in {
      val taxpayersName = Map("forename" -> "Joe", "surname" -> "Bloggs")
      SalutationHelper.salutationFrom(taxpayersName) mustBe "Joe Bloggs"
    }

    "render a salutation with a title and surname" in {
      val taxpayersName = Map("title" -> "Mr", "surname" -> "Bloggs")
      SalutationHelper.salutationFrom(taxpayersName) mustBe "Mr Bloggs"
    }

    "render a salutation with a title, forename and surname" in {
      val taxpayersName =
        Map("title" -> "Mr", "forename" -> "Joe", "surname" -> "Bloggs")
      SalutationHelper.salutationFrom(taxpayersName) mustBe "Mr Joe Bloggs"
    }

    "render a salutation with a forename, surname and honours" in {
      val taxpayersName =
        Map("forename" -> "Joe", "surname" -> "Bloggs", "honours" -> "PhD")
      SalutationHelper.salutationFrom(taxpayersName) mustBe "Joe Bloggs PhD"
    }

    "render a salutation with a forename, second forename, surname and honours" in {
      val taxpayersName =
        Map("forename" -> "Joe", "secondForename" -> "Jerry", "surname" -> "Bloggs", "honours" -> "PhD")
      SalutationHelper.salutationFrom(taxpayersName) mustBe "Joe Jerry Bloggs PhD"
    }

    "render a salutation with a title, surname and honours" in {
      val taxpayersName =
        Map("title" -> "Mr", "surname" -> "Bloggs", "honours" -> "PhD")
      SalutationHelper.salutationFrom(taxpayersName) mustBe "Mr Bloggs PhD"
    }

    "render a salutation with a title, forename, surname and honours" in {
      val taxpayersName = Map("title" -> "Mr", "forename" -> "Joe", "surname" -> "Bloggs", "honours" -> "PhD")
      SalutationHelper.salutationFrom(taxpayersName) mustBe "Mr Joe Bloggs PhD"
    }

    "render a salutation with a title, forename, second forename and surname" in {
      val taxpayersName = Map("title" -> "Mr", "forename" -> "Joe", "secondForename" -> "Jerry", "surname" -> "Bloggs")
      SalutationHelper.salutationFrom(taxpayersName) mustBe "Mr Joe Jerry Bloggs"
    }

    "render a salutation with both title, forename, second forename, surname and honours" in {
      val taxpayersName = Map(
        "title"          -> "Mr",
        "forename"       -> "Joe",
        "secondForename" -> "Jerry",
        "surname"        -> "Bloggs",
        "honours"        -> "PhD"
      )
      SalutationHelper.salutationFrom(taxpayersName) mustBe "Mr Joe Jerry Bloggs PhD"
    }

    "render a default salutation if only a title is present" in {
      val taxpayersName = Map("title" -> "Mr")
      SalutationHelper.salutationFrom(taxpayersName) mustBe "Hi"
    }

  }

}
