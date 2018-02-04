package models.daos

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.LoginInfo
import db.Repository
import exceptions.{InvalidAuthTokenException, UserAlreadyExistsException, UserNotFoundException}
import models._
import models.components.{SilhouetteComponent, UserComponent}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import utils.SqlUtils

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * @author im.
  */
@Singleton
class UserDao @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with UserComponent with SilhouetteComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  /**
    * Finds user by its user id
    *
    * @param userId The id of the user to found
    * @return found user or None if no user for the given user id could be found
    */
  def getById(userId: Long): Future[Option[User]] = {
    val query = sql"SELECT * FROM users_get_by_id(${userId})".as[User].headOption
    db.run(query.asTry).map {
      case Success(res) => res
      case Failure(e: PSQLException) =>
        SqlUtils.parseException(e, UserNotFoundException.throwException)
        throw e
      case Failure(e: Throwable) => throw e
    }
  }

  /**
    * Finds the user by its login info
    *
    * @param loginInfo The login info of the user to find
    * @return The found user or None if no user for the given login info could be found
    */
  def getByLoginInfo(loginInfo: LoginInfo): Future[Option[User]] = {
    Logger.info(loginInfo.toString)
    val query = sql"SELECT * FROM users_get_by_login_info(${loginInfo.providerID}, ${loginInfo.providerKey})".as[User].headOption
    db.run(query.asTry).map {
      case Success(res) => res
      case Failure(e: PSQLException) =>
        SqlUtils.parseException(e, UserNotFoundException.throwException)
        throw e
      case Failure(e: Throwable) => throw e
    }
  }

  def save(email: String, password: String, hasher: String, locale: Option[String], timezone: Option[Int]): Future[User] = {
    val query = sql"SELECT * FROM users_create_auth_password(${email}, ${password}, ${hasher}, ${locale}, ${timezone})".as[User].head
    db.run(query.transactionally.asTry).map {
      case Success(res) => res
      case Failure(e: PSQLException) =>
        SqlUtils.parseException(e, UserAlreadyExistsException.throwException)
        throw e
      case Failure(e: Throwable) => throw e
    }
  }

  def verify(userId: Long, token: UUID) = {
    val query = sql"SELECT * FROM users_verify(${userId}, ${token})".as[User].head
    db.run(query.transactionally.asTry).map {
      case Success(res) => res
      case Failure(e: PSQLException) =>
        SqlUtils.parseException(e, InvalidAuthTokenException.throwException)
        throw e
      case Failure(e: Throwable) => throw e
    }
  }

  def test(roles: List[Long]): Future[Boolean] = {
    db.run(sql"SELECT * FROM test_enum(${roles})".as[Boolean].head)
  }
}
