package models.daos

import java.time.LocalDate

import javax.inject.{Inject, Singleton}
import models.components.PaymentMethodComponent
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.JsValue
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import today.expresso.common.db.Repository
import today.expresso.common.exceptions.PaymentMethodNotFoundException
import today.expresso.common.utils.{SqlUtils, Tx}
import today.expresso.stream.domain.model.payment.PaymentMethod
import today.expresso.stream.domain.model.payment.PaymentMethod.Status.Status
import today.expresso.stream.domain.model.payment.PaymentOption.PaymentOption
import today.expresso.stream.domain.model.payment.PaymentSystem.PaymentSystem

import scala.concurrent.{ExecutionContext, Future}

object PaymentMethodDao {
  implicit val tx: Tx[PaymentMethod] = c => Future.successful(c)
}

@Singleton
class PaymentMethodDao @Inject()(databaseConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with PaymentMethodComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def add(userId: Long,
          paymentOption: PaymentOption,
          paymentSystem: PaymentSystem,
          paymentMethodStatus: Status,
          expirationDate: Option[LocalDate],
          displayName: Option[String],
          isDefault: Boolean,
          firstPaymentDate: Option[LocalDate],
          lastPaymentDate: Option[LocalDate],
          lastFailedDate: Option[LocalDate],
          details: Option[JsValue])(implicit tx: Tx[PaymentMethod]) = {
    val query = sql"SELECT * FROM payment_method_add(${userId}, ${paymentOption}, ${paymentSystem}, ${paymentMethodStatus}, ${expirationDate}, ${displayName}, ${isDefault}, ${firstPaymentDate}, ${lastPaymentDate}, ${lastFailedDate}, ${details})".as[PaymentMethod].head
      .flatMap { paymentMethod =>
        DBIO.from(tx.tx(paymentMethod)).map(_ => paymentMethod)
      }
    db.run(query.transactionally)
  }

  def remove(userId: Long, paymentMethodId: Long)(implicit tx: Tx[PaymentMethod]) = {
    val query = sql"SELECT * FROM payment_method_remove(${userId}, ${paymentMethodId})".as[PaymentMethod].head
      .flatMap { paymentMethod =>
        DBIO.from(tx.tx(paymentMethod)).map(_ => paymentMethod)
      }
    db.run(query.transactionally)
  }

  def update(userId: Long,
             paymentMethodId: Long,
             status: Option[Status],
             details: Option[JsValue],
             displayName: Option[String],
             expirationDate: Option[LocalDate],
             isDefault: Option[Boolean])(implicit tx: Tx[PaymentMethod]) = {
    val query = sql"SELECT * FROM payment_method_update(${userId}, ${paymentMethodId}, ${displayName}, ${isDefault})".as[PaymentMethod].head
      .flatMap { paymentMethod =>
        DBIO.from(tx.tx(paymentMethod)).map(_ => paymentMethod)
      }
    db.run(query.transactionally)
  }

  def successPayment(paymentMethodId: Long, date: LocalDate)(implicit tx: Tx[PaymentMethod]) = {
    val query = sql"SELECT * FROM payment_method_success_payment(${paymentMethodId}, ${date})".as[PaymentMethod].head
      .flatMap { paymentMethod =>
        DBIO.from(tx.tx(paymentMethod)).map(_ => paymentMethod)
      }
    db.run(query.transactionally)
  }

  def failedPayment(paymentMethodId: Long, date: LocalDate)(implicit tx: Tx[PaymentMethod]) = {
    val query = sql"SELECT * FROM payment_method_failed_payment(${paymentMethodId}, ${date})".as[PaymentMethod].head
      .flatMap { paymentMethod =>
        DBIO.from(tx.tx(paymentMethod)).map(_ => paymentMethod)
      }
    db.run(query.transactionally)
  }

  def getByUserIdAndPaymentMethodId(userId: Long, paymentMethodId: Long) = {
    val query = sql"SELECT * FROM payment_method_get(${userId}, ${paymentMethodId}, 'TRUE'::BOOLEAN, 'TRUE'::BOOLEAN)".as[PaymentMethod].head
    db.run(query.asTry).map {
      SqlUtils.tryException(PaymentMethodNotFoundException.throwException)
    }
  }

  def getByUser(userId: Long) = {
    val query = sql"SELECT * FROM payment_method_get(${userId})".as[PaymentMethod]
    db.run(query)
  }

  def search(userId: Long,
             paymentOption: Option[PaymentOption],
             paymentSystem: Option[PaymentSystem],
             details: Option[JsValue],
             successfulOnly: Boolean,
             limit: Int) = {
    val query = sql"SELECT * FROM payment_method_search(${userId}, ${paymentOption}, ${paymentSystem}, ${details}, ${successfulOnly}, ${limit})".as[PaymentMethod]
    db.run(query)
  }

}
