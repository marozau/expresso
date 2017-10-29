package services

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.util.Clock
import models.AuthToken
import models.daos.AuthTokenDAO

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
 * Handles actions to auth tokens.
 *
 * @param authTokenDAO The auth token DAO implementation.
 * @param clock        The clock instance.
 * @param ex           The execution context.
 */
@Singleton
class AuthTokenService @Inject()(
  authTokenDAO: AuthTokenDAO,
  clock: Clock
)(implicit ex: ExecutionContext) {

  /**
   * Creates a new auth token and saves it in the backing store.
   *
   * @param userId The user ID for which the token should be created.
   * @param expiry The duration a token expires.
   * @return The saved auth token.
   */
  def create(userId: Long, expiry: FiniteDuration = 5 minutes): Future[AuthToken] = {
    val token = AuthToken(UUID.randomUUID(), userId, Instant.now().plusSeconds(expiry.toSeconds.toInt))
    authTokenDAO.save(token)
  }

  /**
   * Validates a token ID.
   *
   * @param id The token ID to validate.
   * @return The token if it's valid, None otherwise.
   */
  def validate(id: UUID): Future[Option[AuthToken]] = authTokenDAO.findValid(id)

  /**
   * Cleans expired tokens.
   *
   * @return The list of deleted tokens.
   */
  def clean: Future[Int] = authTokenDAO.removeExpired(Instant.now())
}
