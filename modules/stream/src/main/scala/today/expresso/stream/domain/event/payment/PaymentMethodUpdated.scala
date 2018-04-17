package today.expresso.stream.domain.event.payment

import java.time.LocalDate

import com.sksamuel.avro4s.AvroDoc
import today.expresso.grpc.payment.domain.PaymentMethod.Status
import today.expresso.grpc.payment.domain.{PaymentOption, PaymentSystem}
import today.expresso.stream.api.Key
import today.expresso.stream.domain.Event

case class PaymentMethodUpdated(id: Long,
                                @AvroDoc("Key") @Key userId: Long,
                                paymentOption: PaymentOption,
                                paymentSystem: PaymentSystem,
                                status: Status,
                                expirationDate: Option[LocalDate],
                                displayName: Option[String],
                                deleted: Boolean,
                                isDefault: Boolean,
                                createdDate: LocalDate,
                                firstPaymentDate: Option[LocalDate],
                                lastPaymentDate: Option[LocalDate],
                                lastFailedDate: Option[LocalDate],
                                details: Option[String])
  extends Event
