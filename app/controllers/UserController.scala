package controllers

import javax.inject._

import repositories.UserRepository
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
object UserController {

  case class UserForm(id: Option[Long], email: String, locale: String, timezone: Int)

  val userForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "email" -> email,
      "locale" -> default(nonEmptyText, "ru"),
      "timezone" -> default(number(min = -12, max = 12), 3),
    )(UserForm.apply)(UserForm.unapply)
  )

}

@Singleton
class UserController @Inject()(cc: ControllerComponents, users: UserRepository)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  import UserController._

  def getUserForm(id: Option[Long]) = Action.async { implicit request =>
    def getExisting(id: Long): Future[Form[UserForm]] = {
      users.getById(id)
        .map(user => userForm.fill(UserForm(user.id, user.email, user.locale, user.timezone)))
    }

    def create(): Future[Form[UserForm]] = {
      Future(userForm)
    }

    id.fold(create())(getExisting)
      .map(f => Ok(views.html.admin.user(f)))
  }

  def submitUserForm() = Action.async { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.info(s"bad user, form=$formWithErrors")
        Future(BadRequest(views.html.admin.user(formWithErrors)))
      },
      form => {
        val user = User(None, form.email, form.locale, form.timezone, UserRole.USER, UserStatus.NEW)
        users.create(user)
          .map(u => Ok(Json.toJson(u)))
      }
    )
  }

  //  def create(name: String, role: String) = Action.async {
  //    users.create(name, UserRole.withName(role)).mapTo[User].map(u => Ok(Json.toJson(u)))
  //  }

  def list() = Action.async {
    users.list().map(u => Ok(Json.toJson(u)))
  }
}
