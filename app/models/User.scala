package models

import java.time.ZonedDateTime

import play.api.libs.json._

/**
  * @author im.
  */
object UserRole extends Enumeration {
  val USER, GUEST = Value

  implicit val userRoleReads = Reads.enumNameReads(UserRole)
}

object UserStatus extends Enumeration {
  val NEW, REGISTERED, WAITING_FOR_VERIFICATION, VERIFIED, BLOCKED, CLOSED = Value

  implicit val userStatusFormat = Reads.enumNameReads(UserStatus)
}

case class User(
                 id: Option[Long],
                 email: String,
                 locale: String,
                 timezone: Int,
                 role: UserRole.Value,
                 status: UserStatus.Value,
                 reason: Option[String] = None,
                 createdTimestamp: Option[ZonedDateTime] = None,
                 modifiedTimestamp: Option[ZonedDateTime] = None
               )

object User {

  implicit val personFormat = Json.format[User]
}
