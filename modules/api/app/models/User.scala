package models

import com.mohiva.play.silhouette.api.Identity
import play.api.mvc.PathBindable
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

  final val strToRoleMapper = UserDto.Role.values.map(role => role.name -> role).toMap
  final val strToStatusMapper = UserDto.Status.values.map(status => status.name -> status).toMap

  //TODO: don't work
  implicit def pathUserRoleBindable(implicit stringBinder: PathBindable[String]) = new PathBindable[UserDto.Role] {
    override def bind(key: String, value: String): Either[String, UserDto.Role] = {
      for {
        roleStr <- stringBinder.bind(key, value).right
        role <- User.strToRoleMapper.get(roleStr.toUpperCase).toRight("Role not found").right
      } yield role
    }

    override def unbind(key: String, value: UserDto.Role) = stringBinder.unbind(key, value.name)
  }
}