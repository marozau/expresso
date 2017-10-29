package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.{AbstractController, ControllerComponents}
import events.newsletter.Click
import play.api.Logger
import services.TrackingService

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class TrackingController @Inject()(cc: ControllerComponents, tracking: TrackingService)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  import Click._

//  val click = Click(1,1,1,1)
//  private val str: String = queryStringBindable.unbind("test", click)
//  Logger.info(str)


  def click(data: Click) = Action { implicit request =>
    Logger.info(s"$data")
    // TODO: add utm and utp tags
    // TODO: parse parameters
    // TODO: set cooky
    Redirect("https://www.google.com") //MOVED_PERMANENTLY
  }

//  http://localhost:9000/click?data=AgICAgAAAA%3D%3D
}
