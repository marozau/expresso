package models.daos

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import today.expresso.common.db.Repository
import today.expresso.common.exceptions.UserNotFoundException
import models.components.{CommonComponent, PasswordInfoComponent}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import today.expresso.common.utils.SqlUtils

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
class PasswordInfoDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends DelegableAuthInfoDAO[PasswordInfo] with Repository with PasswordInfoComponent with CommonComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  /**
    * Finds the auth info which is linked with the specified login info.
    *
    * @param loginInfo The linked login info.
    * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
    */
  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    val query = sql"SELECT * FROM password_info_get(${loginInfo.providerID}, ${loginInfo.providerKey})".as[PasswordInfo].headOption
    db.run(query)
  }

  /**
    * Adds new auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be added.
    * @param authInfo  The auth info to add.
    * @return The added auth info.
    */
  def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val query = sql"SELECT * FROM password_info_add(${loginInfo.providerID}, ${loginInfo.providerKey}, ${authInfo.password}, ${authInfo.hasher}, ${authInfo.salt})".as[PasswordInfo].head
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException(UserNotFoundException.throwException)
    }
  }

  /**
    * Updates the auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be updated.
    * @param authInfo  The auth info to update.
    * @return The updated auth info.
    */
  def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val query = sql"SELECT * FROM password_info_update(${loginInfo.providerID}, ${loginInfo.providerKey}, ${authInfo.password}, ${authInfo.hasher}, ${authInfo.salt})".as[PasswordInfo].head
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException(UserNotFoundException.throwException)
    }
  }

  /**
    * Saves the auth info for the given login info.
    *
    * This method either adds the auth info if it doesn't exists or it updates the auth info
    * if it already exists.
    *
    * @param loginInfo The login info for which the auth info should be saved.
    * @param authInfo  The auth info to save.
    * @return The saved auth info.
    */
  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val query = sql"SELECT * FROM password_info_save(${loginInfo.providerID}, ${loginInfo.providerKey}, ${authInfo.password}, ${authInfo.hasher}, ${authInfo.salt})".as[PasswordInfo].head
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException(UserNotFoundException.throwException)
    }
  }

  /**
    * Removes the auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be removed.
    * @return A future to wait for the process to be completed.
    */
  def remove(loginInfo: LoginInfo): Future[Unit] = {
    val query = sql"SELECT * FROM password_info_remove(${loginInfo.providerID}, ${loginInfo.providerKey})".as[Unit].head
    db.run(query)
  }
}
