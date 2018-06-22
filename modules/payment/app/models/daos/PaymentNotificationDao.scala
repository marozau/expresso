package models.daos

import javax.inject.{Inject, Singleton}
import models.components.{CommonComponent, PaymentMethodComponent}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.JsValue
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import today.expresso.common.db.Repository
import today.expresso.stream.domain.model.payment.PaymentSystem.PaymentSystem

import scala.concurrent.ExecutionContext

@Singleton
class PaymentNotificationDao @Inject() (databaseConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with PaymentMethodComponent with CommonComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def saveNotification(key: String, userId: Long, system: PaymentSystem, data: JsValue) = {
    val query = sql"SELECT * FROM payment_notification_add(${key}, ${userId}, ${system}, ${data})".as[Unit].head
    db.run(query.transactionally)
  }

  def getNotification(key: String, userId: Long, system: PaymentSystem) = {
    val query = sql"SELECT * FROM payment_notification_get(${key}, ${userId}, ${system})".as[JsValue].head
    db.run(query)
  }
}
