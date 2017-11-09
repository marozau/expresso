package models.daos

import javax.inject.{Inject, Singleton}

import models.{Newsletter, User}
import models.api.Repository
import models.components.{NewsletterComponent, UserComponent}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class NewsletterDao @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with NewsletterComponent with UserComponent {
  protected val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  implicit def newsletterCast(newsletter: DBNewsletter): Newsletter =
    Newsletter(
      newsletter.id,
      newsletter.userId,
      newsletter.name,
      newsletter.email,
      newsletter.options
    )

  implicit def newsletterOptionCast(newsletter: Option[DBNewsletter]): Option[Newsletter] =
    newsletter.map(newsletterCast)

  def getById(newsletterId: Long) = db.run {
    newsletters.filter(_.id === newsletterId).result.headOption
      .map(newsletterOptionCast)
  }

  def list() = db.run {
    newsletters.result
      .map { result => result.map(newsletterCast) }
  }

  def create(user: User, name: String, email: String) = db.run {
    ((newsletters returning newsletters) += DBNewsletter(None, user.id.get, name, email))
        .map(newsletterCast)
  }
}
