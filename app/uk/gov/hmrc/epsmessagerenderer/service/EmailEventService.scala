/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.service

import uk.gov.hmrc.epsmessagerenderer.connectors.*
import uk.gov.hmrc.epsmessagerenderer.models.{ EmailEvents, PermanentBounce }
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class EmailEventService @Inject() (
  hodsAdapterConnector: HodsAdapterConnector
) {

  def processEventsFor(
    events: EmailEvents,
    nino: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    events.events
      .map(_.event)
      .collectFirst { case PermanentBounce => true }
      .fold[Future[Boolean]](Future.successful(false)) { _ =>
        hodsAdapterConnector.optUserOutOfPrintSuppression(nino)
      }
}
