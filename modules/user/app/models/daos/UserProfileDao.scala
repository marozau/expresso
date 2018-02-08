package models.daos

import javax.inject.{Inject, Singleton}

import db.Repository
import exceptions.UserNotFoundException
import models.UserProfile
import models.components.{CommonComponent, UserProfileComponent}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import utils.SqlUtils

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * @author im.
  */
@Singleton
class UserProfileDao @Inject()(databaseConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with CommonComponent with UserProfileComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def getByUserId(userId: Long) = {
    val query = sql"SELECT * FROM user_profiles_get_by_user_id(${userId})".as[UserProfile].head
    db.run(query.asTry).map {
      case Success(res) => res
      case Failure(e: PSQLException) =>
        SqlUtils.parseException(e, UserNotFoundException.throwException)
        throw e
      case Failure(e: Throwable) => throw e
    }
  }

}
