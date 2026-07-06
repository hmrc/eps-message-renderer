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
