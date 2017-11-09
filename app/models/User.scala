package models

import java.time.ZonedDateTime

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import play.api.libs.json._

/**
  * @author im.
  */
object UserRole extends Enumeration {
  val READER, WRITER, EDITOR, CHIEF_EDITOR, ADMIN = Value

  implicit val userRoleReads = Reads.enumNameReads(UserRole)
}

object UserStatus extends Enumeration {
  val NEW, VERIFIED, BLOCKED = Value

  implicit val userStatusFormat = Reads.enumNameReads(UserStatus)
}

case class User(
                 id: Option[Long],
                 loginInfo: LoginInfo,
                 email: String,
                 roles: List[UserRole.Value],
                 status: UserStatus.Value,
                 locale: Option[String] = None,
                 reason: Option[String] = None,
                 createdTimestamp: Option[ZonedDateTime] = None,
                 modifiedTimestamp: Option[ZonedDateTime] = None
               ) extends Identity

object User {

  implicit val userFormat = Json.format[User]
}
