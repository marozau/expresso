package models.daos

import javax.inject.{Inject, Singleton}

import today.expresso.common.db.Repository
import today.expresso.common.exceptions.{NewsletterAlreadyExistException, NewsletterNotFoundException}
import models.Newsletter
import models.components.NewsletterComponent
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import today.expresso.common.utils.SqlUtils

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * @author im.
  */
@Singleton
class NewsletterDao @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with NewsletterComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def create(userId: Long, name: String, locale: String) = {
    val query = sql"SELECT * FROM newsletters_create(${userId}, ${name}, ${locale})".as[Newsletter].head
    db.run(query.transactionally.asTry).map {
      case Success(res) => res
      case Failure(e: PSQLException) =>
        SqlUtils.parseException(e, NewsletterAlreadyExistException.throwException)
        throw e
      case Failure(e: Throwable) => throw e
    }
  }

  def getById(newsletterId: Long): Future[Newsletter] = {
    val query = sql"SELECT * FROM newsletters_get_by_id(${newsletterId})".as[Newsletter].head
    db.run(query.asTry).map {
      case Success(res) => res
      case Failure(e: PSQLException) =>
        SqlUtils.parseException(e, NewsletterNotFoundException.throwException)
        throw e
      case Failure(e: Throwable) => throw e
    }
  }

  def getByUserId(userId: Long): Future[List[Newsletter]] = {
    val query = sql"SELECT * FROM newsletters_get_by_user(${userId})".as[Newsletter]
    db.run(query).map(_.toList)
  }
}
