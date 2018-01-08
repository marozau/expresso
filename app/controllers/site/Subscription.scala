package controllers.site

import javax.inject.{Inject, Singleton}

import controllers.AssetsFinder
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}

/**
  * @author im.
  */
@Singleton
class Subscription @Inject()(cc: ControllerComponents)(implicit assets: AssetsFinder)
  extends AbstractController(cc) with I18nSupport {

  def error(code: Int) = Action { implicit request =>
    Ok(views.html.email.error(code))
  }

  def thanks() = Action { implicit request =>
    Ok(views.html.email.thanks())
  }
}
