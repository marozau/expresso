package models.daos

import java.util.UUID
import javax.inject.{Inject, Singleton}

import today.expresso.common.db.Repository
import today.expresso.common.exceptions.InvalidAuthTokenException
import models.AuthToken
import models.components.{AuthTokenComponent, CommonComponent}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import today.expresso.common.utils.SqlUtils

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class AuthTokenDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with AuthTokenComponent with CommonComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  /**
    * Saves a token.
    *
    * @param userId The ID of the user who uses token
    * @param expiry Token is valid till expiry timestamp
    * @return The saved token.
    */
  def create(userId: Long, expiry: FiniteDuration): Future[AuthToken] = db.run {
    sql"SELECT * FROM auth_token_create(${userId}, ${expiry})".as[AuthToken].head
  }

  /**
    * Finds a token by its ID.
    *
    * @param id The unique token ID.
    * @return The found token or None if no token for the given ID could be found.
    */
  def find(id: UUID): Future[Option[AuthToken]] = {
    val query = sql"SELECT * FROM auth_token_find(${id})".as[AuthToken].headOption
    db.run(query.asTry).map {
      SqlUtils.tryException(InvalidAuthTokenException.throwException)
    }
  }

  def validate(id: UUID): Future[Option[AuthToken]] = {
    val query = sql"SELECT * FROM auth_token_validate(${id})".as[AuthToken].headOption
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException(InvalidAuthTokenException.throwException)
    }
  }

  /**
    * Removes the token for the given ID.
    *
    * @param id The ID for which the token should be removed.
    * @return A future to wait for the process to be completed.
    */
  def remove(id: UUID): Future[Unit] = db.run {
    sql"SELECT * FROM auth_token_remove(${id})".as[Unit].head.transactionally
  }

  /**
    * Remove all tokens that has expiry >= CURRENT_TIMESTAMP
    *
    * @return
    */
  def clean(): Future[Unit] = db.run {
    sql"SELECT * FROM auth_token_remove_expired()".as[Unit].head.transactionally
  }

}
