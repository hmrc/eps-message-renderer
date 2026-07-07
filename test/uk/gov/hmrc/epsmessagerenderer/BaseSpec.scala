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

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Injecting
import uk.gov.hmrc.domain.{ Nino, NinoGenerator }
import uk.gov.hmrc.epsmessagerenderer.metrics.Metrics
import uk.gov.hmrc.epsmessagerenderer.utils.Auditing
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.metrics.Metrics as KenshooMetrics

import scala.concurrent.ExecutionContext

trait BaseSpec
    extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with Injecting with ScalaFutures
    with BeforeAndAfterEach {

  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      Map(
        "Test.microservice.services.hods-adapter.host" -> "localhost",
        "Test.microservice.services.hods-adapter.port" -> "9412"
      )
    )
    .build()

  lazy val http: HttpClientV2 = inject[HttpClientV2]
  lazy val kenshooMetrics: KenshooMetrics = inject[KenshooMetrics]
  lazy val auditing: Auditing = inject[Auditing]
  lazy val servicesConfig: ServicesConfig = inject[ServicesConfig]

  lazy val mockMetrics: Metrics = mock[Metrics]

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val ec: ExecutionContext = inject[ExecutionContext]

  val nino: Nino = NinoGenerator().nextNino
}
