package models

import java.time.ZonedDateTime

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import play.api.libs.json._

/**
  * @author im.
  */
object UserRole extends Enumeration {
  val USER, WRITER, EDITOR, ADMIN = Value

  implicit val userRoleReads = Reads.enumNameReads(UserRole)
}

// NEW, SUBSCRIBED, UNSUBSCRIBED, BLOCKED, CLEANED are USER statused
// NEW, VERIFIED, BLOCKED are WRITER, EDITOR, ADMIN statuses
object UserStatus extends Enumeration {
  val NEW, SUBSCRIBED, UNSUBSCRIBED, VERIFIED, BLOCKED, CLEANED = Value

  implicit val userStatusFormat = Reads.enumNameReads(UserStatus)
}

case class User(
                 id: Option[Long],
                 loginInfo: LoginInfo,
                 email: String,
                 roles: List[UserRole.Value],
                 status: UserStatus.Value,
                 reason: Option[String] = None,
                 createdTimestamp: Option[ZonedDateTime] = None,
                 modifiedTimestamp: Option[ZonedDateTime] = None
               ) extends Identity

object User {

  implicit val personFormat = Json.format[User]
}
