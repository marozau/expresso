package controllers.user

import javax.inject._

import play.api.i18n.I18nSupport
import play.api.mvc._
import services.UserService

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class UserController @Inject()(cc: ControllerComponents,
                               userService: UserService)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  def list() = Action.async { implicit request: Request[AnyContent] =>
    Future(Ok("sdf"))
    //    users.list().map(u => Ok(Json.toJson(u)))
  }
}
