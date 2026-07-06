/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.controllers

import play.api.libs.json.*
import play.api.mvc.{ Action, ControllerComponents }
import uk.gov.hmrc.epsmessagerenderer.models.EmailEvents
import uk.gov.hmrc.epsmessagerenderer.service.EmailEventService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{ Inject, Singleton }
import scala.concurrent.ExecutionContext

@Singleton
class EmailEventController @Inject() (
  emailEventService: EmailEventService
)(implicit ec: ExecutionContext, cc: ControllerComponents)
    extends BackendController(cc) {

  def processEvent(nino: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    implicit val eventsReads: Reads[EmailEvents] = EmailEvents.reads
    withJsonBody[EmailEvents] { events =>
      emailEventService
        .processEventsFor(events, nino)
        .map(b => if (b) Ok("User opted out of print suppression") else NoContent)
    }
  }
}
