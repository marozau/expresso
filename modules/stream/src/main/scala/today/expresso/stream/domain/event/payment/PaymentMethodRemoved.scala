package today.expresso.stream.domain.event.payment

import com.sksamuel.avro4s.AvroDoc
import today.expresso.stream.api.Key
import today.expresso.stream.domain.model.payment.PaymentMethod
import today.expresso.stream.domain.model.payment.PaymentSystem.PaymentSystem
import today.expresso.stream.domain.{Event, Serializer}
import today.expresso.stream.serde.utils.SpecificAvroUtils

case class PaymentMethodRemoved(id: Long, @AvroDoc("Key") @Key userId: Long, system: PaymentSystem) extends Event

object PaymentMethodRemoved extends Serializer[PaymentMethodRemoved] {
  override def toBinary(t: PaymentMethodRemoved) = SpecificAvroUtils.serialize[PaymentMethodRemoved](t)
  override def fromBinary(bytes: Array[Byte]) = SpecificAvroUtils.deserialize[PaymentMethodRemoved](bytes)

  def apply(m: PaymentMethod): PaymentMethodRemoved = PaymentMethodRemoved(m.id, m.userId, m.system)
}