package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.routing._

/**
  * @author im.
  */
@Singleton
class Application @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.PostController.getPostForm
      )
    ).as("text/javascript")
  }

}
