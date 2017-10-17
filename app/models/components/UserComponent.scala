package models.components

import java.time.ZonedDateTime

import models._
import models.repositories.Repository
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

  protected class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def email = column[String]("email", O.Unique)

    def locale = column[String]("locale")

    def timezone = column[Int]("timezone")

    def role = column[UserRole.Value]("role")

    def status = column[UserStatus.Value]("status")

    def reason = column[Option[String]]("reason")

    def createdTimestamp = column[ZonedDateTime]("created_timestamp", SqlUtils.timestampTzNotNullType)

    def modifiedTimestamp = column[ZonedDateTime]("modified_timestamp", SqlUtils.timestampTzNotNullType)

    def * = (id.?, email, locale, timezone, role, status, reason, createdTimestamp.?, modifiedTimestamp.?) <> ((User.apply _).tupled, User.unapply)

  }

  protected val users = TableQuery[Users]
}
