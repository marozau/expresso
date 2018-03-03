package config

import db.Repository
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
    Await.result(
      db.run(sql"TRUNCATE qrtz_fired_triggers, qrtz_paused_trigger_grps, qrtz_scheduler_state, qrtz_locks, qrtz_simple_triggers, qrtz_cron_triggers, qrtz_simprop_triggers, qrtz_blob_triggers, qrtz_triggers, qrtz_job_details, qrtz_calendars CASCADE".as[Unit].head), 5.seconds
    )
  }
}
