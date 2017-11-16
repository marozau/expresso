package models.components

import java.time.{LocalDate, ZonedDateTime}

import models.api.Repository
import play.api.libs.json.JsValue
import utils.SqlUtils

/**
  * @author im.
  */

trait EditionComponent {
  this: Repository with NewsletterComponent =>

  import api._

  case class DBEdition(id: Option[Long],
                       newsletterId: Long,
                       date: LocalDate,
                       url: Option[String] = None,
                       title: Option[String] = None,
                       header: Option[String] = None,
                       footer: Option[String] = None,
                       postIds: List[Long] = List.empty,
                       options: Option[JsValue] = None,
                       createdTimestamp: Option[ZonedDateTime] = None,
                       modifiedTimestamp: Option[ZonedDateTime] = None)

  protected class Editions(tag: Tag) extends Table[DBEdition](tag, "editions") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def newsletterId = column[Long]("newsletter_id")

    def date = column[LocalDate]("date")

    def url = column[Option[String]]("url")

    def title = column[Option[String]]("title")

    def header = column[Option[String]]("header")

    def footer = column[Option[String]]("footer")

    def postIds = column[List[Long]]("post_ids")

    def options = column[Option[JsValue]]("options")

    def createdTimestamp = column[ZonedDateTime]("created_timestamp", SqlUtils.timestampTzNotNullType)

    def modifiedTimestamp = column[ZonedDateTime]("modified_timestamp", SqlUtils.timestampTzNotNullType)

    def * = (id.?, newsletterId, date, url, title, header, footer, postIds, options, createdTimestamp.?, modifiedTimestamp.?) <> ((DBEdition.apply _).tupled, DBEdition.unapply)

    def newsletterIdSupplier = foreignKey("editions_newsletter_id_fkey", newsletterId, newsletters)(_.id)
  }

  protected val editions = TableQuery[Editions]
}
