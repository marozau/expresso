package today.expresso.stream.domain.model.payment

import java.time.LocalDate

import play.api.libs.json.JsValue
import today.expresso.stream.domain.model.payment.PaymentMethod.Status.Status
import today.expresso.stream.domain.model.payment.PaymentOption.PaymentOption
import today.expresso.stream.domain.model.payment.PaymentSystem.PaymentSystem

object PaymentMethod {

  object Status extends Enumeration {
    type Status = Value
    val OK = Value
    val INREVIEW = Value
    val INCOMPLETE = Value
    val BLOCKED = Value
  }

}

case class PaymentMethod(id: Long,
                         userId: Long,
                         option: PaymentOption,
                         system: PaymentSystem,
                         status: Status,
                         expirationDate: Option[LocalDate],
                         displayName: Option[String],
                         deleted: Boolean,
                         isDefault: Boolean,
                         createdDate: LocalDate,
                         firstPaymentDate: Option[LocalDate],
                         lastPaymentDate: Option[LocalDate],
                         lastFailedDate: Option[LocalDate],
                         details: Option[JsValue])
