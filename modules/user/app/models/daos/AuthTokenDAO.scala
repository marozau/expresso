package models.daos

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}

import db.Repository
import models.AuthToken
import models.components.{AuthTokenComponent, CommonComponent, UserComponent}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

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
    * Finds a token by its ID.
    *
    * @param id The unique token ID.
    * @return The found token or None if no token for the given ID could be found.
    */
  def find(id: UUID): Future[Option[AuthToken]] = db.run {
    sql"SELECT * FROM auth_token_find(${id})".as[AuthToken].headOption
  }

  def findValid(id: UUID): Future[Option[AuthToken]] = db.run {
    sql"SELECT * FROM auth_token_find_valid(${id})".as[AuthToken].headOption
  }


  def removeExpired(timestamp: Instant): Future[Unit] = db.run {
    sql"SELECT * FROM auth_token_remove_expired()".as[Unit].head
  }

  /**
    * Saves a token.
    *
    * @param userId The ID of the user who uses token
    * @param expiry Token is valid till expiry timestamp
    * @return The saved token.
    */
  def create(userId: Long, expiry: Instant): Future[AuthToken] = db.run {
    sql"SELECT * FROM auth_token_save(${userId}, ${expiry})".as[AuthToken].head
  }

  /**
    * Removes the token for the given ID.
    *
    * @param id The ID for which the token should be removed.
    * @return A future to wait for the process to be completed.
    */
  def remove(id: UUID): Future[Unit] = db.run {
    sql"SELECT * FROM auth_token_remove(${id})".as[Unit].head
  }
}
