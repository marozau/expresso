package controllers

import javax.inject.{Inject, Singleton}

import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}

/**
  * @author im.
  */
@Singleton
class Application @Inject()(cc: ControllerComponents)
  extends AbstractController(cc) {

  //TODO: add healthcehck endpoint with all systems status - database, message bus, external services etc
  def healthz() = Action {
    Ok("ok")
  }
}
