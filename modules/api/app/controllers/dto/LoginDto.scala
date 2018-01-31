package controllers.dto

import play.api.libs.json.Json

/**
  * @author im.
  */
case class LoginDto(email: String, password: String)

object LoginDto {

  implicit val loginDtoFormat = Json.format[LoginDto]
}
