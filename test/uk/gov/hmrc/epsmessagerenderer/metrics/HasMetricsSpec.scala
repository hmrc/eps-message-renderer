/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.metrics

import com.codahale.metrics.Timer
import uk.gov.hmrc.play.bootstrap.metrics.{ Metrics => KenshooMetrics }
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{ times, when }
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

class HasMetricsSpec extends PlaySpec with MockitoSugar {

  trait SetUp {
    class TestHasMetrics extends HasMetrics {
      val timer = mock[Timer.Context]
      val metricId = "test"

      override def metrics: KenshooMetrics = mock[KenshooMetrics]

      override val metricsOperator = mock[MetricsOperator]
      when(metricsOperator.startTimer(any[Metric])) thenReturn timer

      def testCompleteTimerAndIncrementSuccessCounter(): Unit =
        withMetricsTimer(metricId) { t =>
          t.completeTimerAndIncrementSuccessCounter()

          val inOrder = Mockito.inOrder(metricsOperator, timer)

          inOrder.verify(metricsOperator, times(1)).startTimer(metricId)
          inOrder.verify(timer, times(1)).stop()
          inOrder
            .verify(metricsOperator, times(1))
            .incrementSuccessCounter(metricId)
        }

      def testCompleteTimerAndIncrementFailedCounter(): Unit =
        withMetricsTimer(metricId) { t =>
          t.completeTimerAndIncrementFailedCounter()

          val inOrder = Mockito.inOrder(metricsOperator, timer)

          inOrder.verify(metricsOperator, times(1)).startTimer(metricId)
          inOrder.verify(timer, times(1)).stop()
          inOrder
            .verify(metricsOperator, times(1))
            .incrementFailedCounter(metricId)
        }
    }
  }

  "completeTimerAndIncrementSuccessCounter should start/stop the timer and increment the success counter in a specific order" in new SetUp {
    new TestHasMetrics().testCompleteTimerAndIncrementSuccessCounter()
  }

  "completeTimerAndIncrementFailedCounter should start/stop the timer and increment the failed counter in a specific order" in new SetUp {
    new TestHasMetrics().testCompleteTimerAndIncrementFailedCounter()
  }
}
