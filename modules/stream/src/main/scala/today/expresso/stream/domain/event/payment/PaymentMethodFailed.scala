package today.expresso.stream.domain.event.payment

import com.sksamuel.avro4s.AvroDoc
import today.expresso.stream.api.Key
import today.expresso.stream.domain.model.payment.PaymentMethod
import today.expresso.stream.domain.model.payment.PaymentSystem.PaymentSystem
import today.expresso.stream.domain.{Event, Serializer}
import today.expresso.stream.serde.utils.SpecificAvroUtils

case class PaymentMethodFailed(id: Long, @AvroDoc("Key") @Key userId: Long, system: PaymentSystem) extends Event

object PaymentMethodFailed extends Serializer[PaymentMethodFailed] {
  override def toBinary(t: PaymentMethodFailed) = SpecificAvroUtils.serialize[PaymentMethodFailed](t)
  override def fromBinary(bytes: Array[Byte]) = SpecificAvroUtils.deserialize[PaymentMethodFailed](bytes)

  def apply(m: PaymentMethod): PaymentMethodFailed = PaymentMethodFailed(m.id, m.userId, m.system)
}
