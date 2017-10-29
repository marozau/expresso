package controllers.auth

import javax.inject._

import com.mohiva.play.silhouette.api.Silhouette
import models.UserRole
import modules.DefaultEnv
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.UserService
import utils.WithRole

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class UserController @Inject()(cc: ControllerComponents,
                               silhouette: Silhouette[DefaultEnv],
                               userService: UserService)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  def list() = silhouette.SecuredAction(WithRole(UserRole.ADMIN)).async { implicit request: Request[AnyContent] =>
    Future(Ok("sdf"))
    //    users.list().map(u => Ok(Json.toJson(u)))
  }
}
