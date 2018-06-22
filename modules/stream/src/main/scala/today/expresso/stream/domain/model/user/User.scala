package today.expresso.stream.domain.model.user

import java.time.Instant

import today.expresso.stream.domain.model.user.User.Role.Role
import today.expresso.stream.domain.model.user.User.Status.Status


object User {

  object Role extends Enumeration {
    type Role = Value
    val USER, READER, MEMBER, WRITER, EDITOR, CHIEF_EDITOR, ADMIN, API = Value
  }

  object Status extends Enumeration {
    type Status = Value
    val NEW, VERIFIED, BLOCKED = Value
  }

}

case class User(id: Long,
                email: String,
                status: Status,
                roles: List[Role],
                locale: Option[String],
                timezone: Option[Int],
                reason: Option[String],
                createdTimestamp: Instant)
