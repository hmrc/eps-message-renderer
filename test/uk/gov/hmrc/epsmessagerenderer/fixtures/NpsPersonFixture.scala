/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.fixtures

import uk.gov.hmrc.epsmessagerenderer.models.nps.NpsPerson

object NpsPersonFixture {

  def getValidPersonObject(nino: String): NpsPerson =
    NpsPerson(nino, "Mr", "A", "Smith")

}
