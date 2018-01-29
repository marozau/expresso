package models

import java.time.ZonedDateTime

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import play.api.libs.json._

/**
  * @author im.
  */

object User {
  object Role extends Enumeration {
    val USER, READER, MEMBER, WRITER, EDITOR, CHIEF_EDITOR, ADMIN = Value

    implicit val userRoleReads = Reads.enumNameReads(User.Role)
  }

  object Status extends Enumeration {
    val NEW, VERIFIED, BLOCKED = Value

    implicit val userStatusReads = Reads.enumNameReads(User.Status)
  }

  implicit val userFormat = Json.format[User]
}

case class User(
                 id: Option[Long],
                 loginInfo: LoginInfo,
                 email: String,
                 roles: List[User.Role.Value],
                 status: User.Status.Value,
                 locale: Option[String] = None,
                 timezone: Option[Int] = None,
                 reason: Option[String] = None,
                 createdTimestamp: Option[ZonedDateTime] = None,
                 modifiedTimestamp: Option[ZonedDateTime] = None
               ) extends Identity

