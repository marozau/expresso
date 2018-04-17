package models.daos

import javax.inject.{Inject, Singleton}
import models.Currency
import models.components.CurrencyComponent
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import today.expresso.common.db.Repository

import scala.concurrent.ExecutionContext

@Singleton
class CurrencyDao @Inject() (databaseConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with CurrencyComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def getCurrencyAll() = {
    val query = sql"SELECT * FROM currency".as[Currency]
    db.run(query)
  }
}
