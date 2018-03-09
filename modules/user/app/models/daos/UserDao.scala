package models.daos

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.LoginInfo
import today.expresso.common.db.Repository
import today.expresso.common.exceptions.{InvalidAuthTokenException, UserAlreadyExistsException, UserNotFoundException}
import models._
import models.components.{CommonComponent, UserComponent}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import today.expresso.common.utils.SqlUtils

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class UserDao @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with UserComponent with CommonComponent {

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
      SqlUtils.tryException(UserNotFoundException.throwException)
    }
  }

  /**
    * Finds the user by its login info
    *
    * @param loginInfo The login info of the user to find
    * @return The found user or None if no user for the given login info could be found
    */
  def getByLoginInfo(loginInfo: LoginInfo): Future[Option[User]] = {
    val query = sql"SELECT * FROM users_get_by_login_info(${loginInfo.providerID}, ${loginInfo.providerKey})".as[User].headOption
    db.run(query.asTry).map {
      SqlUtils.tryException(UserNotFoundException.throwException)
    }
  }

  def create(email: String, password: String, hasher: String, salt: Option[String], locale: Option[String], timezone: Option[Int]): Future[User] = {
    val query = sql"SELECT * FROM users_create_auth_password(${email}, ${password}, ${hasher}, ${salt}, ${locale}, ${timezone})".as[User].head
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException(UserAlreadyExistsException.throwException)
    }
  }

  def verify(userId: Long, token: UUID) = {
    val query = sql"SELECT * FROM users_verify(${userId}, ${token})".as[User].head
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException(InvalidAuthTokenException.throwException)
    }
  }

  def createReader(email: String, locale: Option[String]) = {
    val query = sql"SELECT * FROM users_create(${email}, ${locale}, ${Option.empty[Int]}, ${List(User.Role.READER)})".as[User].head
    db.run(query.transactionally.asTry).map{
      SqlUtils.tryException(UserAlreadyExistsException.throwException)
    }
  }
}
