package models.daos

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.LoginInfo
import db.Repository
import exceptions.UserAlreadyExistsException
import models._
import models.components.{SilhouetteComponent, UserComponent}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.{JdbcProfile, SetParameter}
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
    val userQuery = for {
      dbUser <- users.filter(_.id === userId)
      dbUserLoginInfo <- userLoginInfos.filter(_.userId === userId)
      dbLoginInfo <- loginInfos.filter(_.id === dbUserLoginInfo.loginInfoId)
    } yield (dbUser, dbLoginInfo)
    db.run(userQuery.result.headOption).map { resultOption =>
      resultOption
        .map { case (user, loginInfo) =>
          User(
            user.id,
            LoginInfo(loginInfo.providerId, loginInfo.providerKey),
            user.email,
            user.roles,
            user.status,
            user.locale,
            user.timezone,
            user.reason,
            user.createdTimestamp,
            user.modifiedTimestamp
          )
        }
    }
  }

  /**
    * Finds the user by its login info
    *
    * @param loginInfo The login info of the user to find
    * @return The found user or None if no user for the given login info could be found
    */
  def getByLoginInfo(loginInfo: LoginInfo): Future[Option[User]] = {
    val userQuery = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbUserLoginInfo <- userLoginInfos.filter(_.loginInfoId === dbLoginInfo.id)
      dbUser <- users.filter(_.id === dbUserLoginInfo.userId)
    } yield dbUser
    db.run(userQuery.result.headOption).map { userOption =>
      userOption.map { user =>
        User(
          user.id,
          loginInfo,
          user.email,
          user.roles,
          user.status,
          user.locale,
          user.timezone,
          user.reason,
          user.createdTimestamp,
          user.modifiedTimestamp
        )
      }
    }
  }

  /**
    * Saves a user
    *
    * @param user The user to save
    * @return newly saved or updated user
    */
  def save(user: User): Future[User] = {
    val dbLoginInfo = DBLoginInfo(None, user.loginInfo.providerID, user.loginInfo.providerKey)
    val dbUser = DBUser(user.id, user.email, user.roles, user.status, user.locale, user.timezone, user.reason)
    val loginInfoAction = {
      val retrieveLoginInfo = loginInfos.filter(
        info => info.providerId === user.loginInfo.providerID &&
          info.providerKey === user.loginInfo.providerKey).result.headOption
      val insertLoginInfo = loginInfos.returning(loginInfos.map(_.id)).
        into((info, id) => info.copy(id = Some(id))) += dbLoginInfo
      for {
        loginInfoOption <- retrieveLoginInfo
        loginInfo <- loginInfoOption.map(DBIO.successful).getOrElse(insertLoginInfo)
      } yield loginInfo
    }

    val insertUserAction: DBIOAction[DBUser, NoStream, Effect.Write] = (users returning users) += dbUser
    val updateUserAction: DBIOAction[DBUser, NoStream, Effect.Write] = (users returning users).insertOrUpdate(dbUser).map(_.getOrElse(dbUser))
    val actions = (for {
      newUser <- user.id.fold(insertUserAction)(_ => updateUserAction)
      loginInfo <- loginInfoAction
      _ <- userLoginInfos += DBUserLoginInfo(newUser.id.get, loginInfo.id.get)
    } yield user.copy(id = newUser.id, createdTimestamp = newUser.createdTimestamp, modifiedTimestamp = newUser.modifiedTimestamp)).transactionally
    db.run(actions.transactionally.withPinnedSession)
  }

  /**
    * Get database user list without login info
    *
    * @param size  The size of the list to return
    * @param offset The offset from the first user to include in the result list
    * @return database user list
    */
  def list(size: Int = Int.MaxValue, offset: Int = 0): Future[Seq[DBUser]] = db.run {
    users
      .sortBy(_.createdTimestamp)
      .take(size)
      .drop(offset)
      .result
  }

  def verify(userId: Long) = db.run {
    users
      .filter(user => user.id === userId && user.status === User.Status.NEW)
      .map(_.status)
      .update(User.Status.VERIFIED)
  }

  def create(email: String, password: String, hasher: String, locale: Option[String], timezone: Option[Int]): Future[Long] = {
    val query = sql"SELECT * FROM users_create_auth_password(${email}, ${password}, ${hasher}, ${locale}, ${timezone})".as[Long].head
    db.run(query.asTry).map {
      case Success(res) => res
      case Failure(e: PSQLException) =>
        SqlUtils.parseException(e, UserAlreadyExistsException.throwException)
        throw e
      case Failure(e: Throwable) => throw e
    }
  }

  implicit val setUserRole: SetParameter[User.Role.Value] = SetParameter { (t, pp) => userRoleTypeMapper.setValue(t, pp.ps, pp.pos + 1)}
  implicit val setUserRoles: SetParameter[List[User.Role.Value]] = SetParameter { (t, pp) => userRoleListTypeMapper.setValue(t, pp.ps, pp.pos + 1)}
  implicit val setUserRoles2: SetParameter[List[Long]] = SetParameter { (t, pp) => simpleLongListTypeMapper.setValue(t, pp.ps, pp.pos + 1)}

//  implicit val userRoleTypeMapper1 = createEnumJdbcType("user_role", User.Role)
//  userRoleTypeMapper.

  def test(roles: List[Long]): Future[Boolean] = {
    db.run(sql"SELECT * FROM test_enum(${roles})".as[Boolean].head)
  }
}
