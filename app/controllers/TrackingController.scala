package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.{AbstractController, ControllerComponents}
import services.Tracking

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class TrackingController @Inject() (cc: ControllerComponents, tracking: Tracking)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def click() = Action { implicit request =>
    // TODO: parse parameters
    // TODO: set cooky
    Redirect("https://www.google.com") //MOVED_PERMANENTLY
  }
}
