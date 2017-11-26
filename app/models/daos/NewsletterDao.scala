package models.daos

import java.net.URL
import javax.inject.{Inject, Singleton}

import exceptions.{NewsletterAlreadyExistException, NewsletterNameUrlAlreadyExistException, NewsletterNotFoundException}
import models.Newsletter
import models.api.Repository
import models.components.{NewsletterComponent, UserComponent}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Lang
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import utils.SqlUtils.PostgreSQLErrorCodes

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

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
      .map { result =>
        result.map(newsletterCast)
      }
  }

  def create(newsletter: Newsletter) = {
    val query = ((newsletters returning newsletters) += DBNewsletter(None, newsletter.userId, newsletter.name, newsletter.nameUrl, newsletter.email, newsletter.lang.code))
      .map(newsletterCast)
    db.run(query.asTry).map {
      case Success(res) => res
      case Failure(e: PSQLException) =>
        //TODO: need to implement same common error handling mechanism to reuse
        //TODO: db.run(query.asTry).map with error handling code need to be moved to the Repository
        if (e.getSQLState == PostgreSQLErrorCodes.UniqueViolation.toString) {
          if (e.getMessage.contains(newsletters.baseTableRow.newslettersNameIdx.name)) {
            throw NewsletterAlreadyExistException(e.getMessage)
          } else if (e.getMessage.contains(newsletters.baseTableRow.newslettersNameUrlIdx.name)) {
            throw NewsletterNameUrlAlreadyExistException(e.getMessage)
          } else {
            throw e
          }
        }
        throw e
    }
  }
}
