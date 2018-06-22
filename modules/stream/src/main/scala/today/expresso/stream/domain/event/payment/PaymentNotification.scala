package today.expresso.stream.domain.event.payment

import com.sksamuel.avro4s.AvroDoc
import today.expresso.stream.api.Key
import today.expresso.stream.domain.model.payment.PaymentStatus.PaymentStatus
import today.expresso.stream.domain.model.payment.PaymentSystem.PaymentSystem
import today.expresso.stream.domain.model.payment.PaymentType.PaymentType
import today.expresso.stream.domain.{Event, Serializer}
import today.expresso.stream.serde.utils.SpecificAvroUtils

case class PaymentNotification(externalTxId: String,
                               system: PaymentSystem,
                               ptype: PaymentType,
                               @AvroDoc("key") @Key userId: Long,
                               accountId: Long,
                               amount: BigDecimal,
                               commission: BigDecimal,
                               currency: String,
                               paymentMethod: String,
                               paymentMethodId: Long,
                               paymentMethodDisplayName: String,
                               status: PaymentStatus,
                               note: String,
                               timestamp: Long,
                               details: Option[String])
  extends Event

object PaymentNotification extends Serializer[PaymentNotification] {
  override def toBinary(t: PaymentNotification) = SpecificAvroUtils.serialize[PaymentNotification](t)
  override def fromBinary(bytes: Array[Byte]) = SpecificAvroUtils.deserialize[PaymentNotification](bytes)
}