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

package uk.gov.hmrc.epsmessagerenderer.connectors

import play.api.Logging
import play.api.http.Status.*
import play.api.libs.json.{ JsValue, Json }
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.epsmessagerenderer.metrics.HasMetrics
import uk.gov.hmrc.epsmessagerenderer.models.*
import uk.gov.hmrc.epsmessagerenderer.models.nps.NpsPrintSuppressionUpdateRequest
import uk.gov.hmrc.epsmessagerenderer.models.nps.NpsPrintSuppressionUpdateRequest.{ OutputPreference, PayeFormType }
import uk.gov.hmrc.epsmessagerenderer.utils.Auditing
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{ HeaderCarrier, HttpReads, HttpResponse }
import uk.gov.hmrc.play.audit.model.EventTypes
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.metrics.Metrics as KenshooMetrics

import java.net.{ URI, URL }
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class HodsAdapterConnector @Inject() (
  val metrics: KenshooMetrics,
  http: HttpClientV2,
  servicesConfig: ServicesConfig,
  auditing: Auditing
) extends HasMetrics with Logging {

  def baseUrl: String = servicesConfig.baseUrl("hods-adapter")

  def url(path: String): URL = new URI(s"$baseUrl/eps-hods-adapter$path").toURL

  def statusUrl(path: String): URL = new URI(s"$baseUrl$path").toURL

  def setWorkItemStatus(path: String, processingStatus: NotificationStatus)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Boolean] =
    withMetricsTimer("set-work-item-status") { timer =>
      http
        .post(statusUrl(path))
        .withBody(Json.toJson(processingStatus))
        .execute[HttpResponse]
        .map { response =>
          response.status match {
            case NO_CONTENT =>
              auditing
                .createAudit[NotificationStatus](EventTypes.Succeeded, "Notification status was set", processingStatus)
              timer.completeTimerAndIncrementSuccessCounter()
              true
            case _ =>
              auditing
                .createAudit[NotificationStatus](EventTypes.Failed, "Notification status was not set", processingStatus)
              timer.completeTimerAndIncrementFailedCounter()
              false
          }
        }
    }

  def getPerson(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    withMetricsTimer("get-person") { timer =>
      http
        .get(url(s"/preferences/person/$nino"))
        .execute[HttpResponse]
        .map { response =>
          response.status match {
            case OK =>
              timer.completeTimerAndIncrementSuccessCounter()
              response
            case status =>
              timer.completeTimerAndIncrementFailedCounter()
              response
          }
        }
    }

  def optUserOutOfPrintSuppression(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    withMetricsTimer("opt-user-out-of-print-suppression") { timer =>
      val suppressionRequest =
        NpsPrintSuppressionUpdateRequest(PayeFormType.p2, OutputPreference.paper, bounced = true)
      http
        .post(url(s"/preferences/person/$nino/print-suppression"))
        .withBody(Json.toJson(suppressionRequest))
        .execute[HttpResponse]
        .map { response =>
          response.status match {
            case OK =>
              timer.completeTimerAndIncrementSuccessCounter()
              auditing.createAudit[NpsPrintSuppressionUpdateRequest](
                EventTypes.Succeeded,
                "User opted out",
                suppressionRequest
              )
              true
            case _ =>
              timer.completeTimerAndIncrementFailedCounter()
              auditing.createAudit[NpsPrintSuppressionUpdateRequest](
                EventTypes.Failed,
                "User could not be opted out",
                suppressionRequest
              )
              false
          }
        }
    }

  // Implicit reads for Read the raw response
  implicit val httpReads: HttpReads[HttpResponse] =
    new HttpReads[HttpResponse] {
      override def read(method: String, url: String, response: HttpResponse): HttpResponse =
        response
    }
}
