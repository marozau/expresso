package models.daos

import javax.inject.{Inject, Singleton}
import models.PaymentMethod.PaymentSystem.PaymentSystem
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.JsValue
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import today.expresso.common.db.Repository

import scala.concurrent.ExecutionContext

@Singleton
class PaymentNotificationDao @Inject() (databaseConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def saveNotification(key: String, userId: Long, paymentSystem: PaymentSystem, data: JsValue) = {
    val query = sql"SELECT * FROM payment_notification_add(${key}, ${userId}, ${paymentSystem}, ${data})".as[Unit].head
    db.run(query.transactionally)
  }

  def getNotification(key: String, userId: Long, paymentSystem: PaymentSystem) = {
    val query = sql"SELECT * FROM payment_notification_get(${key}, ${userId}, ${paymentSystem})".as[JsValue].head
    db.run(query)
  }
}
