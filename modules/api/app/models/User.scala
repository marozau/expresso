package models

import com.mohiva.play.silhouette.api.Identity
import today.expresso.grpc.user.dto.{UserDto, UserIdentityDto}

/**
  * @author im.
  */
case class User(
                 id: Long,
                 status: UserDto.Status,
                 roles: Seq[UserDto.Role],
                 locale: Option[String] = None,
               ) extends Identity

object User {

  implicit def userCast(user: UserIdentityDto): User = {
    User(
      user.id,
      user.status,
      user.roles,
      if (user.locale.isEmpty) None else Some(user.locale))
  }

  implicit def userIdentityDtoCast(user: User): UserIdentityDto = {
    UserIdentityDto(
      user.id,
      user.status,
      user.roles,
      user.locale.getOrElse(""))
  }
}