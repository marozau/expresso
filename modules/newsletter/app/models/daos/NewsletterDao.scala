package models.daos

import javax.inject.{Inject, Singleton}

import db.Repository
import exceptions.{AuthorizationException, NewsletterAlreadyExistException, NewsletterNotFoundException}
import models.components.{CommonComponent, NewsletterComponent}
import models.{Locale, Newsletter}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.JsValue
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import utils.SqlUtils

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class NewsletterDao @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with NewsletterComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def create(userId: Long, name: String, locale: Locale.Value): Future[Newsletter] = {
    val query = sql"SELECT * FROM newsletters_create(${userId}, ${name}, ${locale})".as[Newsletter].head
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException(
        NewsletterAlreadyExistException.throwException
      )
    }
  }

  def update(userId: Long,
             newsletterId: Long,
             locale: Option[Locale.Value],
             logoUrl: Option[String],
             avatarUrl: Option[String],
             options: Option[JsValue]): Future[Newsletter] = {
    val query = sql"SELECT * FROM newsletters_update(${userId}, ${newsletterId}, ${locale}, ${logoUrl}, ${avatarUrl}, ${options})".as[Newsletter].head
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException(
        AuthorizationException.throwException,
        NewsletterAlreadyExistException.throwException,
      )
    }
  }

  def getById(userId: Long, newsletterId: Long): Future[Newsletter] = {
    val query = sql"SELECT * FROM newsletters_get_by_id(${userId}, ${newsletterId})".as[Newsletter].head
    db.run(query.asTry).map {
      SqlUtils.tryException(
        AuthorizationException.throwException,
        NewsletterNotFoundException.throwException
      )
    }
  }

  def getByUserId(userId: Long): Future[List[Newsletter]] = {
    val query = sql"SELECT * FROM newsletters_get_by_user_id(${userId})".as[Newsletter]
    db.run(query).map(_.toList)
  }

  def validateName(name: String): Future[Boolean] = {
    val query = sql"SELECT * FROM newsletters_name_validate(${name})".as[Boolean].head
    db.run(query)
  }

  def validateNameUrl(nameUrl: String): Future[Boolean] = {
    val query = sql"SELECT * FROM newsletters_name_url_validate(${nameUrl})".as[Boolean].head
    db.run(query)
  }
}
