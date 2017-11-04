package models.components

import java.time.ZonedDateTime

import models.Post
import models.api.Repository
import play.api.libs.json.JsValue
import utils.SqlUtils

/**
  * @author im.
  */
trait PostComponent {
  this: Repository with UserComponent with EditionComponent =>

  import api._

  protected class Posts(tag: Tag) extends Table[Post](tag, "posts") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("user_id")

    def editionId = column[Option[Long]]("edition_id")

    def title = column[String]("title")

    def annotation = column[String]("annotation")

    def body = column[String]("body")

    def refs = column[List[String]]("refs")

    def options = column[Option[JsValue]]("options")

    def createdTimestamp = column[ZonedDateTime]("created_timestamp", SqlUtils.timestampTzNotNullType)

    def modifiedTimestamp = column[ZonedDateTime]("modified_timestamp", SqlUtils.timestampTzNotNullType)

    def * = (id.?, userId, editionId, title, annotation, body, refs, options, createdTimestamp.?, modifiedTimestamp.?) <> ((Post.apply _).tupled, Post.unapply)

    def userIdSupplier = foreignKey("posts_user_id_fkey", userId, users)(_.id)

    def newsletterIdSupplier = foreignKey("posts_newsletter_id_fkey", editionId, editions)(_.id.?)
  }

  protected val posts = TableQuery[Posts]
}
