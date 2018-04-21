package models

import java.time.LocalDate

import play.api.libs.json.JsValue
import today.expresso.stream.api.ToEvent
import today.expresso.stream.domain.event.payment._

object PaymentMethod {

  object PaymentOption extends Enumeration {
    type PaymentOption = Value

    val BANK_CARD = Value
    val YANDEX_WALLET = Value
    val APPLE_PAY = Value
    val SBERBANK = Value
    val QIWI = Value
    val WEBMONEY = Value
    val CASH = Value
    val MOBILE_BALANCE = Value
    val ALFABANK = Value
  }

  object PaymentSystem extends Enumeration {
    type PaymentSystem = Value

    val YANDEX = Value
  }

  object Status extends Enumeration {
    type Status = Value

    val OK = Value
    val INREVIEW = Value
    val INCOMPLETE = Value
    val BLOCKED = Value
  }

  implicit object ToPaymentMethodAdded extends ToEvent[PaymentMethod, PaymentMethodAdded] {
    override def apply(t: PaymentMethod) =
      PaymentMethodAdded(
        t.id,
        t.userId,
        today.expresso.grpc.payment.domain.PaymentOption.fromName(t.paymentOption.toString).get,
        today.expresso.grpc.payment.domain.PaymentSystem.fromName(t.paymentSystem.toString).get,
        today.expresso.grpc.payment.domain.PaymentMethod.Status.fromName(t.status.toString).get,
        t.expirationDate,
        t.displayName,
        t.deleted,
        t.isDefault,
        t.createdDate,
        t.firstPaymentDate,
        t.lastPaymentDate,
        t.lastFailedDate,
        t.details.map(_.toString))
  }

  implicit object ToPaymentMethodUpdated extends ToEvent[PaymentMethod, PaymentMethodUpdated] {
    override def apply(t: PaymentMethod) =
      PaymentMethodUpdated(
        t.id,
        t.userId,
        today.expresso.grpc.payment.domain.PaymentOption.fromName(t.paymentOption.toString).get,
        today.expresso.grpc.payment.domain.PaymentSystem.fromName(t.paymentSystem.toString).get,
        today.expresso.grpc.payment.domain.PaymentMethod.Status.fromName(t.status.toString).get,
        t.expirationDate,
        t.displayName,
        t.deleted,
        t.isDefault,
        t.createdDate,
        t.firstPaymentDate,
        t.lastPaymentDate,
        t.lastFailedDate,
        t.details.map(_.toString))
  }

  implicit object ToPaymentMethodRemoved extends ToEvent[PaymentMethod, PaymentMethodRemoved] {
    override def apply(t: PaymentMethod): PaymentMethodRemoved = {
      PaymentMethodRemoved(
        t.id,
        t.userId
      )
    }
  }

  implicit object ToPaymentMethodSuccess extends ToEvent[PaymentMethod, PaymentMethodSuccess] {
    override def apply(t: PaymentMethod): PaymentMethodSuccess = {
      PaymentMethodSuccess(
        t.id,
        t.userId
      )
    }
  }

  implicit object ToPaymentMethodFailed extends ToEvent[PaymentMethod, PaymentMethodFailed] {
    override def apply(t: PaymentMethod): PaymentMethodFailed = {
      PaymentMethodFailed(
        t.id,
        t.userId
      )
    }
  }
}

case class PaymentMethod(id: Long,
                         userId: Long,
                         paymentOption: PaymentMethod.PaymentOption.Value,
                         paymentSystem: PaymentMethod.PaymentSystem.Value,
                         status: PaymentMethod.Status.Value,
                         expirationDate: Option[LocalDate],
                         displayName: Option[String],
                         deleted: Boolean,
                         isDefault: Boolean,
                         createdDate: LocalDate,
                         firstPaymentDate: Option[LocalDate],
                         lastPaymentDate: Option[LocalDate],
                         lastFailedDate: Option[LocalDate],
                         details: Option[JsValue])