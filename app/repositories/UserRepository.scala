package repositories

import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}

import models._
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with UserComponent with UserProfileComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import api._

  def create(email: String, role: UserRole.Value): Future[User] = db.run {
    (users.map(u => (u.email, u.locale, u.role, u.status))
      returning users.map(_.id)
      into ((user, id) => User(id, user._1, user._2, user._3, user._4, None, ZonedDateTime.now(), ZonedDateTime.now()))
      ) += (email, "ru", role, UserStatus.NEW)
  }

  def list(): Future[Seq[User]] = db.run {
    users.result
  }
}
