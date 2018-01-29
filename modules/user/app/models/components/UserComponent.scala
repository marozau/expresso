package models.components

import java.time.ZonedDateTime

import db.Repository
import models._
import utils.SqlUtils

/**
  * @author im.
  */
trait UserComponent {
  this: Repository =>

  import api._

  implicit val userRoleTypeMapper = createEnumJdbcType("user_role", User.Role)
  implicit val userRoleListTypeMapper = createEnumListJdbcType("user_role", User.Role)
  implicit val userRoleColumnExtensionMethodsBuilder = createEnumColumnExtensionMethodsBuilder(User.Role)
  implicit val userStatusTypeMapper = createEnumJdbcType("user_status", User.Status)
  implicit val userStatusListTypeMapper = createEnumListJdbcType("user_status", User.Status)
  implicit val userStatusColumnExtensionMethodsBuilder = createEnumColumnExtensionMethodsBuilder(User.Status)

  case class DBUser(
                   id: Option[Long],
                   email: String,
                   roles: List[User.Role.Value],
                   status: User.Status.Value,
                   locale: Option[String] = None,
                   timezone: Option[Int] = None,
                   reason: Option[String] = None,
                   createdTimestamp: Option[ZonedDateTime] = None,
                   modifiedTimestamp: Option[ZonedDateTime] = None
                 )

  protected class Users(tag: Tag) extends Table[DBUser](tag, "users") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def email = column[String]("email", O.Unique)

    def roles = column[List[User.Role.Value]]("roles")

    def status = column[User.Status.Value]("status")

    def locale = column[Option[String]]("locale")

    def timezone = column[Option[Int]]("timezone")

    def reason = column[Option[String]]("reason")

    def createdTimestamp = column[ZonedDateTime]("created_timestamp", SqlUtils.timestampTzNotNullType)

    def modifiedTimestamp = column[ZonedDateTime]("modified_timestamp", SqlUtils.timestampTzNotNullType)

    def * = (id.?, email, roles, status, locale, timezone, reason, createdTimestamp.?, modifiedTimestamp.?) <> ((DBUser.apply _).tupled, DBUser.unapply)

  }

  protected val users = TableQuery[Users]
}
