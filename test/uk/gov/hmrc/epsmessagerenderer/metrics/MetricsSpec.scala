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

package uk.gov.hmrc.epsmessagerenderer.metrics

import java.util.concurrent.TimeUnit
import com.codahale.metrics.MetricRegistry
import org.scalatestplus.play.PlaySpec
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.play.bootstrap.metrics.{ Metrics => KenshooMetrics }

class MetricsSpec extends PlaySpec with MockitoSugar {

  "Metrics" must {
    "record NPS get person response timer" in {
      val mockKenshooMetrics = mock[KenshooMetrics]
      val registry = new MetricRegistry()
      when(mockKenshooMetrics.defaultRegistry).thenReturn(registry)

      val metrics = new Metrics(mockKenshooMetrics)

      metrics.npsGetPersonResponseTimer(100L, TimeUnit.MILLISECONDS)

      val timer = registry.timer("nps-get-person-response-timer")
      timer.getCount mustBe 1
    }

    "increment sent email counter" in {
      val mockKenshooMetrics = mock[KenshooMetrics]
      val registry = new MetricRegistry()
      when(mockKenshooMetrics.defaultRegistry).thenReturn(registry)

      val metrics = new Metrics(mockKenshooMetrics)

      metrics.sentEmailCount()
      metrics.sentEmailCount()

      val counter = registry.counter("sent-email-count")
      counter.getCount mustBe 2
    }

    "use the registry from KenshooMetrics" in {
      val mockKenshooMetrics = mock[KenshooMetrics]
      val registry = new MetricRegistry()
      when(mockKenshooMetrics.defaultRegistry).thenReturn(registry)

      val metrics = new Metrics(mockKenshooMetrics)

      metrics.registry mustBe registry
    }
  }
}
