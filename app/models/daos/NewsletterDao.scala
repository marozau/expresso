package models.daos

import java.net.URL
import javax.inject.{Inject, Singleton}

import models.Newsletter
import models.api.Repository
import models.components.{NewsletterComponent, UserComponent}
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Lang
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
      newsletter.nameUrl,
      newsletter.email,
      Lang(newsletter.locale),
      newsletter.logoUrl.map(new URL(_)),
      newsletter.options
    )

  implicit def newsletterOptionCast(newsletter: Option[DBNewsletter]): Option[Newsletter] =
    newsletter.map(newsletterCast)

  def getById(newsletterId: Long) = db.run {
    newsletters.filter(_.id === newsletterId).result.headOption
      .map(newsletterOptionCast)
  }

  def getByNameUrl(nameUrl: String) = db.run {
    newsletters.filter(_.nameUrl === nameUrl).result.headOption
      .map(newsletterOptionCast)
  }

  def list() = db.run {
    newsletters.result
      .map { result => result.map(newsletterCast) }
  }

  def create(newsletter: Newsletter) = db.run {
    ((newsletters returning newsletters) += DBNewsletter(None, newsletter.userId, newsletter.name, newsletter.nameUrl, newsletter.email, newsletter.lang.code))
        .map(newsletterCast)
  }
}
