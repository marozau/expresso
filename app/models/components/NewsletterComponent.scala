package models.components

import java.time.ZonedDateTime

import models.api.Repository
import play.api.libs.json.JsValue
import utils.SqlUtils

/**
  * @author im.
  */
trait NewsletterComponent {
  this: Repository with UserComponent =>

  import api._

  case class DBNewsletter(id: Option[Long],
                          userId: Long,
                          name: String,
                          email: String,
                          options: Option[JsValue] = None,
                          createdTimestamp: Option[ZonedDateTime] = None,
                          modifiedTimestamp: Option[ZonedDateTime] = None)

  protected class Newsletters(tag: Tag) extends Table[DBNewsletter](tag, "newsletters") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("user_id")

    def name = column[String]("name")

    def email = column[String]("email")

    def options = column[Option[JsValue]]("options")

    def createdTimestamp = column[ZonedDateTime]("created_timestamp", SqlUtils.timestampTzNotNullType)

    def modifiedTimestamp = column[ZonedDateTime]("modified_timestamp", SqlUtils.timestampTzNotNullType)

    def * = (id.?, userId, name, email, options, createdTimestamp.?, modifiedTimestamp.?) <> ((DBNewsletter.apply _).tupled, DBNewsletter.unapply)

    def userIdSupplier = foreignKey("newsletters_user_id_fkey", userId, users)(_.id)
  }

  protected val newsletters = TableQuery[Newsletters]
}
