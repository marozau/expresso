package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.{AbstractController, ControllerComponents}
import events.newsletter.{Click, Subscribe, Unsubscribe}
import play.api.Logger
import services.TrackingService

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class EventController @Inject()(cc: ControllerComponents, app: models.Application)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def click(data: Click) = Action { implicit request =>
    Logger.info(s"$data")
    // TODO: add utm and utp tags
    // TODO: parse parameters
    // TODO: set cooke
    Redirect("https://www.google.com")
  }

//  http://localhost:9000/click?data=AgICAgAAAA%3D%3D

//  //TODO: redirect to /confirmed
  def subscribe(data: Subscribe) = Action.async { implicit request =>
//    (for {
//      _ <- app.userService.verify(data.userId)
//      _ <- app.recipientService.verify(data.newsletterId, data.userId)
//    } yield ()).map { _ =>
//      Redirect("https://www.google.com")
//    }
    Future.successful(Redirect("https://www.google.com"))
  }

//  TODO: unsubscribe and redirect to
  def unsubscribe(data: Unsubscribe) = Action.async { implicit request =>
    app.recipientService.unsubscribe(data.newsletterId, data.userId)
      .map(_ => Redirect("https://www.google.com"))
  }
}
