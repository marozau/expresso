package config

import today.expresso.common.db.Repository
import models.components.CommonComponent
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author im.
  */
class TestDatabase(dbConfigProvider: DatabaseConfigProvider) extends Repository with CommonComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def cleanAll(): Unit = {
    Logger.info("cleaning test database tables")
    Await.result(
      db.run(sql"TRUNCATE newsletters, editions, posts, campaigns, newsletter_writers, edition_writers, recipients CASCADE".as[Unit].head), 5.seconds
    )
  }
}
