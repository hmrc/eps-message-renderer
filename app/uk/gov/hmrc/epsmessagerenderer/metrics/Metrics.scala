/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.metrics

import java.util.concurrent.TimeUnit

import com.codahale.metrics.MetricRegistry
import uk.gov.hmrc.play.bootstrap.metrics.{ Metrics => KenshooMetrics }
import javax.inject.Inject

class Metrics @Inject() (val metrics: KenshooMetrics) {

  lazy val registry: MetricRegistry = metrics.defaultRegistry

  def npsGetPersonResponseTimer(diff: Long, unit: TimeUnit): Unit =
    registry.timer("nps-get-person-response-timer").update(diff, unit)

  def sentEmailCount(): Unit = registry.counter("sent-email-count").inc()

}
