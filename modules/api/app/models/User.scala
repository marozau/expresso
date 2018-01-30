package models

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import play.api.libs.json.{Json, Reads, Writes}
import today.expresso.grpc.user.dto.{UserDto, UserIdentityDto}

/**
  * @author im.
  */
case class User(
                 id: Long,
                 roles: Seq[UserDto.Role],
                 status: UserDto.Status,
                 locale: Option[String] = None,
               ) extends Identity

object User {

  implicit def userCast(user: UserIdentityDto): User = {
    User(
      user.id,
      user.roles,
      user.status,
      if (user.locale.isEmpty) None else Some(user.locale))
  }

  implicit def userIdentityDtoCast(user: User): UserIdentityDto = {
    UserIdentityDto(
      user.id,
      user.roles,
      user.status,
      user.locale.getOrElse(""))
  }
}