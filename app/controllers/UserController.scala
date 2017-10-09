package controllers

import javax.inject._

import repositories.UserRepository
import models._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
class UserController @Inject()(cc: ControllerComponents, repo: UserRepository)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def create(name: String, role: String) = Action.async {
    repo.create(name, UserRole.withName(role)).mapTo[User].map(u => Ok(Json.toJson(u)))
  }

  def list() = Action.async {
    repo.list().map(u => Ok(Json.toJson(u)))
  }
}
