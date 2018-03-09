package models.daos

import javax.inject.{Inject, Singleton}

import today.expresso.common.db.Repository
import today.expresso.common.exceptions.UserNotFoundException
import models.UserProfile
import models.components.{CommonComponent, UserProfileComponent}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import today.expresso.common.utils.SqlUtils

import scala.concurrent.ExecutionContext

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
        SqlUtils.tryException(UserNotFoundException.throwException)
    }
  }

}
