package services

import java.util.UUID
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import models.AuthToken
import models.daos.AuthTokenDAO
import play.api.Logger

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Handles actions to auth tokens.
  *
  * @param authTokenDAO The auth token DAO implementation.
  * @param ex           The execution context.
  */
@Singleton
class AuthTokenService @Inject()(
                                  authTokenDAO: AuthTokenDAO,
                                  system: ActorSystem
                                )(implicit ex: ExecutionContext) {


  system.scheduler.schedule(1.hour, 1.hour, new Runnable {
    override def run(): Unit = clean
  })

  /**
    * Creates a new auth token and saves it in the backing store.
    *
    * @param userId The user ID for which the token should be created.
    * @param expiry The duration a token expires.
    * @return The saved auth token.
    */
  def create(userId: Long, expiry: FiniteDuration = 5 minutes): Future[AuthToken] = {
    authTokenDAO.create(userId, expiry)
  }

  /**
    * Validates a token ID.
    *
    * @param id The token ID to validate.
    * @return The token if it's valid, None otherwise.
    */
  def validate(id: UUID): Future[Option[AuthToken]] = {
    authTokenDAO.validate(id)
  }

  /**
    * Cleans expired tokens.
    *
    * @return The list of deleted tokens.
    */
  def clean: Future[Unit] = {
    Logger.info("clean expired auth tokens")
    authTokenDAO.clean()
  }
}
