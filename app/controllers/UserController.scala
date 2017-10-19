package controllers

import javax.inject._

import models.daos.UserDao
import models._
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class UserController @Inject()(cc: ControllerComponents, users: UserDao)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  def list() = Action.async {
    Future(Ok("sdf"))
//    users.list().map(u => Ok(Json.toJson(u)))
  }
}
