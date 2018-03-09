package models.daos

import java.net.URL
import java.time.LocalDate
import javax.inject.{Inject, Singleton}

import today.expresso.common.db.Repository
import today.expresso.common.exceptions._
import models.Edition
import models.components.EditionComponent
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.JsValue
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import today.expresso.common.utils.SqlUtils

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * @author im.
  */
@Singleton
class EditionDao @Inject()(databaseConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with EditionComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def create(userId: Long, newsletterId: Long, date: LocalDate): Future[Edition] = {
    val query = sql"SELECT * FROM editions_create(${userId}, ${newsletterId}, ${date})".as[Edition].head
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException(
        NewsletterNotFoundException.throwException,
        EditionAlreadyExistsException.throwException
      )
    }
  }

  def update(userId: Long,
             editionId: Long,
             date: Option[LocalDate],
             url: Option[String],
             title: Option[String],
             header: Option[JsValue],
             footer: Option[JsValue],
             options: Option[JsValue]): Future[Edition] = {
    val query = sql"SELECT * FROM editions_update(${userId}, ${editionId}, ${date}, ${url}, ${title}, ${header}, ${footer}, ${options})".as[Edition].head
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException(
        AuthorizationException.throwException,
        EditionNotFoundException.throwException
      )
    }
  }

  def removeUrl(userId: Long, editionId: Long): Future[Edition] = {
    val query = sql"SELECT * FROM editions_remove_url(${userId}, ${editionId})".as[Edition].head
    db.run(query.asTry).map {
      SqlUtils.tryException(
        AuthorizationException.throwException,
        EditionNotFoundException.throwException
      )
    }
  }

  def getById(userId: Long, editionId: Long): Future[Edition] = {
    val query = sql"SELECT * FROM editions_get_by_id(${userId}, ${editionId})".as[Edition].head
    db.run(query.asTry).map {
      SqlUtils.tryException(
        AuthorizationException.throwException,
        EditionNotFoundException.throwException
      )
    }
  }

  def getByNewsletterId(userId: Long, newsletterId: Long): Future[List[Edition]] = {
    val query = sql"SELECT * FROM editions_get_by_newsletter_id(${userId}, ${newsletterId})".as[Edition]
    db.run(query.asTry).map {
      case Success(res) => res.toList
      case Failure(e: PSQLException) =>
        SqlUtils.parseException(e, NewsletterNotFoundException.throwException)
        throw e
      case Failure(e: Throwable) => throw e
    }
  }
}
