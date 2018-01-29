package models.components

import java.time.ZonedDateTime

import db.Repository
import play.api.libs.json.JsValue
import utils.SqlUtils

/**
  * @author im.
  */
trait NewsletterComponent {
  this: Repository =>

  import api._

  case class DBNewsletter(id: Option[Long],
                          userId: Long,
                          name: String,
                          nameUrl: String,
                          email: String,
                          locale: String,
                          logoUrl: Option[String] = None,
                          options: Option[JsValue] = None,
                          createdTimestamp: Option[ZonedDateTime] = None,
                          modifiedTimestamp: Option[ZonedDateTime] = None)

  protected class Newsletters(tag: Tag) extends Table[DBNewsletter](tag, "newsletters") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("user_id")

    def name = column[String]("name")

    def nameUrl = column[String]("name_url")

    def email = column[String]("email")

    def locale = column[String]("locale")

    def logoUrl = column[Option[String]]("logo_url")

    def options = column[Option[JsValue]]("options")

    def createdTimestamp = column[ZonedDateTime]("created_timestamp", SqlUtils.timestampTzNotNullType)

    def modifiedTimestamp = column[ZonedDateTime]("modified_timestamp", SqlUtils.timestampTzNotNullType)

    def * = (id.?, userId, name, nameUrl, email, locale, logoUrl, options, createdTimestamp.?, modifiedTimestamp.?) <> ((DBNewsletter.apply _).tupled, DBNewsletter.unapply)
  }

  protected val newsletters = TableQuery[Newsletters]
}
