package models

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import play.api.libs.json.{Json, Reads, Writes}
import today.expresso.grpc.user.dto.UserDto

/**
  * @author im.
  */
object User {
//  implicit val userRoleReads = Reads.enumNameReads(UserDto.Role)
//  implicit val userRoleFormat = Json.format[UserDto.Role]
//  implicit val userSeqRoleFormat = Json.format[Seq[UserDto.Role]]
//
////  implicit val userStatusReads = Reads.enumNameReads(UserDto.Status)
//  implicit val userStatusFormat = Json.format[UserDto.Status]
//
//  implicit val userFormat = Json.format[User]
}

case class User(
                 id: Long,
                 loginInfo: LoginInfo,
                 roles: Seq[UserDto.Role],
                 status: UserDto.Status,
                 locale: Option[String] = None,
               ) extends Identity