/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.epsmessagerenderer.controllers

import play.api.libs.json.*
import play.api.mvc.{ Action, ControllerComponents }
import uk.gov.hmrc.epsmessagerenderer.models.PayeNotificationWorkItem
import uk.gov.hmrc.epsmessagerenderer.service.PayeAlerter
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{ Inject, Singleton }
import scala.concurrent.ExecutionContext

@Singleton
class AlertController @Inject() (
  alertService: PayeAlerter,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def processNotification(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[PayeNotificationWorkItem] { workItem =>
      alertService
        .processNotification(workItem)
        .map(b => if (b) Accepted else InternalServerError)
    }
  }
}
