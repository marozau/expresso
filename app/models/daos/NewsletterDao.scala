package models.daos

import javax.inject.{Inject, Singleton}

import models.{Newsletter, User}
import models.api.Repository
import models.components.{NewsletterComponent, UserComponent}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class NewsletterDao @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with NewsletterComponent with UserComponent {
  protected val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def list() = db.run {
    newsletters.result
      .map { result =>
        result.map { newsletter =>
          Newsletter(
            newsletter.id,
            newsletter.userId,
            newsletter.name,
            newsletter.options
          )
        }
      }
  }

  def create(user: User, name: String) = db.run {
    ((newsletters returning newsletters) += DBNewsletter(None, user.id.get, name))
      .map{newsletter =>
        Newsletter(
          newsletter.id,
          newsletter.userId,
          newsletter.name,
          newsletter.options)
      }
  }
}
