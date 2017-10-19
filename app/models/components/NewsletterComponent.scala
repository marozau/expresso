package models.components

import java.time.ZonedDateTime

import models.Newsletter
import models.api.Repository
import play.api.libs.json.JsValue
import utils.SqlUtils

/**
  * @author im.
  */

trait NewsletterComponent {
  this: Repository with UserComponent =>

  import api._

  protected class Newsletters(tag: Tag) extends Table[Newsletter](tag, "newsletters") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("user_id")

    def url = column[Option[String]]("url")

    def title = column[Option[String]]("title")

    def header = column[Option[String]]("header")

    def footer = column[Option[String]]("footer")

    def postIds = column[List[Long]]("post_ids")

    def options = column[Option[JsValue]]("options")

    def publishTimestamp = column[Option[ZonedDateTime]]("publish_timestamp", SqlUtils.timestampTzType)

    def createdTimestamp = column[ZonedDateTime]("created_timestamp", SqlUtils.timestampTzNotNullType)

    def modifiedTimestamp = column[ZonedDateTime]("modified_timestamp", SqlUtils.timestampTzNotNullType)

    def * = (id.?, userId, url, title, header, footer, postIds, options, publishTimestamp, createdTimestamp.?, modifiedTimestamp.?) <> ((Newsletter.apply _).tupled, Newsletter.unapply)

    def userIdSupplier = foreignKey("newsletters_user_id_fkey", userId, users)(_.id)
  }

  protected val newsletters = TableQuery[Newsletters]
}
