package models.daos

import java.time.{Instant, ZoneOffset, ZonedDateTime}
import java.util.UUID
import javax.inject.{Inject, Singleton}

import db.Repository
import models.AuthToken
import models.components.{SilhouetteComponent, UserComponent}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class AuthTokenDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with SilhouetteComponent with UserComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  implicit def authTokenCast(token: DBAuthToken): AuthToken = AuthToken(token.id, token.userId, token.expiry.toInstant)

  implicit def authTokenOptionCast(token: Option[DBAuthToken]): Option[AuthToken] = token.map(authTokenCast)

  implicit def authTokenSeqCast(tokens: Seq[DBAuthToken]): Seq[AuthToken] = tokens.map(authTokenCast)

  implicit def dbAuthTokenCast(token: AuthToken): DBAuthToken = DBAuthToken(token.id, token.userId, ZonedDateTime.ofInstant(token.expiry, ZoneOffset.UTC))

  /**
    * Finds a token by its ID.
    *
    * @param id The unique token ID.
    * @return The found token or None if no token for the given ID could be found.
    */
  def find(id: UUID): Future[Option[AuthToken]] = db.run {
    authTokens.filter(_.id === id).result.headOption
      .map(authTokenOptionCast)
  }

  def findValid(id: UUID): Future[Option[AuthToken]] = db.run {
    authTokens.filter(token => token.id === id && token.expiry > ZonedDateTime.now(ZoneOffset.UTC)).result.headOption
      .map(authTokenOptionCast)
  }

  /**
    * Finds expired tokens.
    *
    * @param timestamp The current date time.
    */
  def findExpired(timestamp: Instant): Future[Seq[AuthToken]] = db.run {
    authTokens.filter(_.expiry <= ZonedDateTime.ofInstant(timestamp, ZoneOffset.UTC)).result
      .map(authTokenSeqCast)
  }

  def removeExpired(timestamp: Instant): Future[Int] = db.run {
    authTokens.filter(_.expiry <= ZonedDateTime.ofInstant(timestamp, ZoneOffset.UTC)).delete
  }

  /**
    * Saves a token.
    *
    * @param token The token to save.
    * @return The saved token.
    */
  def save(token: AuthToken): Future[AuthToken] = db.run {
    ((authTokens returning authTokens) += token).map(authTokenCast)
  }

  /**
    * Removes the token for the given ID.
    *
    * @param id The ID for which the token should be removed.
    * @return A future to wait for the process to be completed.
    */
  def remove(id: UUID): Future[Int] = db.run {
    authTokens.filter(_.id === id).delete
  }
}
