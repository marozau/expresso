package models.components

import java.time.ZonedDateTime

import models._
import models.api.Repository
import utils.SqlUtils

/**
  * @author im.
  */
trait UserComponent {
  this: Repository =>

  import api._

  implicit val userRoleTypeMapper = createEnumJdbcType("user_role", UserRole)
  implicit val userRoleListTypeMapper = createEnumListJdbcType("user_role", UserRole)
  implicit val userRoleColumnExtensionMethodsBuilder = createEnumColumnExtensionMethodsBuilder(UserRole)
  implicit val userStatusTypeMapper = createEnumJdbcType("user_status", UserStatus)
  implicit val userStatusListTypeMapper = createEnumListJdbcType("user_status", UserStatus)
  implicit val userStatusColumnExtensionMethodsBuilder = createEnumColumnExtensionMethodsBuilder(UserStatus)

  case class DBUser(
                   id: Option[Long],
                   email: String,
                   roles: List[UserRole.Value],
                   status: UserStatus.Value,
                   locale: Option[String] = None,
                   reason: Option[String] = None,
                   createdTimestamp: Option[ZonedDateTime] = None,
                   modifiedTimestamp: Option[ZonedDateTime] = None
                 )

  protected class Users(tag: Tag) extends Table[DBUser](tag, "users") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def email = column[String]("email", O.Unique)

    def roles = column[List[UserRole.Value]]("roles")

    def status = column[UserStatus.Value]("status")

    def locale = column[Option[String]]("locale")

    def reason = column[Option[String]]("reason")

    def createdTimestamp = column[ZonedDateTime]("created_timestamp", SqlUtils.timestampTzNotNullType)

    def modifiedTimestamp = column[ZonedDateTime]("modified_timestamp", SqlUtils.timestampTzNotNullType)

    def * = (id.?, email, roles, status, locale, reason, createdTimestamp.?, modifiedTimestamp.?) <> ((DBUser.apply _).tupled, DBUser.unapply)

  }

  protected val users = TableQuery[Users]
}
