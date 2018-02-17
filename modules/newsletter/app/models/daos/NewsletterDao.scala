package models.daos

import java.net.URL
import javax.inject.{Inject, Singleton}

import db.Repository
import exceptions.{AuthorizationException, NewsletterAlreadyExistException, NewsletterNotFoundException}
import models.Newsletter
import models.components.NewsletterComponent
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Lang
import play.api.libs.json.JsValue
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import utils.SqlUtils

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

  def create(userId: Long, name: String, locale: Lang): Future[Newsletter] = {
    val query = sql"SELECT * FROM newsletters_create(${userId}, ${name}, ${locale})".as[Newsletter].head
    db.run(query.transactionally.asTry).map {
      case Success(res) => res
      case Failure(e: PSQLException) =>
        SqlUtils.parseException(e, NewsletterAlreadyExistException.throwException)
        throw e
      case Failure(e: Throwable) => throw e
    }
  }

  def update(userId: Long,
             newsletterId: Long,
             locale: Option[Lang],
             logoUrl: Option[URL],
             avatarUrl: Option[URL],
             options: Option[JsValue]): Future[Newsletter] = {
    val query = sql"SELECT * FROM newsletters_update(${userId}, ${newsletterId}, ${locale}, ${logoUrl}, ${avatarUrl}, ${options})".as[Newsletter].head
    db.run(query.transactionally.asTry).map {
      case Success(res) => res
      case Failure(e: PSQLException) =>
        SqlUtils.parseException(e, AuthorizationException.throwException)
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
