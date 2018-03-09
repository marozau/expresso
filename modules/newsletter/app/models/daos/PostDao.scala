package models.daos

import javax.inject.{Inject, Singleton}

import today.expresso.common.db.Repository
import today.expresso.common.exceptions._
import models.Post
import models.components.PostComponent
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
class PostDao @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with PostComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def create(userId: Long,
             editionId: Long,
             editionOrder: Int,
             title: String,
             annotation: String,
             body: JsValue,
             options: Option[JsValue]): Future[Post] = {
    val query = sql"SELECT * FROM posts_create(${userId}, ${editionId}, ${editionOrder}, ${title}, ${annotation}, ${body}, ${options})".as[Post].head
    db.run(query.transactionally.asTry).map {
      case Success(res) => res
      case Failure(e: PSQLException) =>
        SqlUtils.parseException(e, PostTitleDuplicationException.throwException)
        throw e
      case Failure(e: Throwable) => throw e
    }
  }

  def update(userId: Long,
             postId: Long,
             editionOrder: Option[Int],
             title: Option[String],
             annotation: Option[String],
             body: Option[JsValue],
             options: Option[JsValue]): Future[Post] = {
    val query = sql"SELECT * FROM posts_create(${userId}, ${postId}, ${editionOrder}, ${title}, ${annotation}, ${body}, ${options})".as[Post].head
    db.run(query.transactionally.asTry).map {
      case Success(res) => res
      case Failure(e: PSQLException) =>
        SqlUtils.parseException(e, AuthorizationException.throwException)
        SqlUtils.parseException(e, PostNotFoundException.throwException)
        SqlUtils.parseException(e, PostTitleDuplicationException.throwException)
        throw e
      case Failure(e: Throwable) => throw e
    }
  }

  def getById(userId: Long, postId: Long): Future[Post] = {
    val query = sql"SELECT * FROM posts_get_by_id(${userId}, ${postId})".as[Post].head
    db.run(query.transactionally.asTry).map {
      case Success(res) => res
      case Failure(e: PSQLException) =>
        SqlUtils.parseException(e, AuthorizationException.throwException)
        SqlUtils.parseException(e, PostNotFoundException.throwException)
        SqlUtils.parseException(e, PostTitleDuplicationException.throwException)
        throw e
      case Failure(e: Throwable) => throw e
    }
  }
}
