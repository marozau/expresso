package controllers

import javax.inject._

import com.mohiva.play.silhouette.api.Silhouette
import modules.AuthEnv
import play.api.i18n.I18nSupport
import play.api.mvc._
import today.expresso.grpc.user.dto.UserDto
import utils.WithRole

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class UserController @Inject()(cc: ControllerComponents,
                               silhouette: Silhouette[AuthEnv])(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  def list() = silhouette.SecuredAction(WithRole(UserDto.Role.ADMIN)).async { implicit request: Request[AnyContent] =>
    Future(Ok("sdf"))
    //    users.list().map(u => Ok(Json.toJson(u)))
  }
}
