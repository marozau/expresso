package controllers

import javax.inject.{Inject, Singleton}

import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.routing._

/**
  * @author im.
  */
@Singleton
class Application @Inject()(cc: ControllerComponents) (implicit assets: AssetsFinder)
  extends AbstractController(cc) with I18nSupport {

  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        auth.routes.javascript.AuthController.index
      )
    ).as("text/javascript")
  }


  def healthz() = Action {
    Ok("ok")
  }

  def error(code: Int) = Action { implicit request =>
    Ok(views.html.email.error(code))
  }
}
