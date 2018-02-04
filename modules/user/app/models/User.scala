package models

import java.time.Instant

import com.mohiva.play.silhouette.api.Identity
import play.api.libs.json._

/**
  * @author im.
  */

object User {
  object Role extends Enumeration {
    val USER, READER, MEMBER, WRITER, EDITOR, CHIEF_EDITOR, ADMIN, API = Value

    implicit val userRoleReads = Reads.enumNameReads(User.Role)
  }

  object Status extends Enumeration {
    val NEW, VERIFIED, BLOCKED = Value

    implicit val userStatusReads = Reads.enumNameReads(User.Status)
  }

  implicit val userFormat = Json.format[User]
}

case class User(
                 id: Long,
                 status: User.Status.Value,
                 roles: List[User.Role.Value],
                 locale: Option[String],
                 timezone: Option[Int],
                 reason: Option[String],
                 createdTimestamp: Instant
               ) extends Identity

