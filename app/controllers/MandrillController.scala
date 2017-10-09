package controllers

import javax.inject.{Inject, Singleton}

import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}
import services.Mandrill

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class MandrillController @Inject()(cc: ControllerComponents, mandrill: Mandrill)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  // mandrill.sendEmail("marozau.ih@gmail.com", "Expresso news", Map())
  def send(email: String, template: String) = Action.async { implicit request =>
    mandrill.sendEmail(email, template) //TODO: parse parameters from request
      .map(u => Ok(u)) //TODO: html template
  }
}
