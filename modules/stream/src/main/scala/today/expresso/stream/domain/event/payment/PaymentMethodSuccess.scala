package today.expresso.stream.domain.event.payment

import com.sksamuel.avro4s.AvroDoc
import today.expresso.stream.api.Key
import today.expresso.stream.domain.model.payment.PaymentMethod
import today.expresso.stream.domain.model.payment.PaymentSystem.PaymentSystem
import today.expresso.stream.domain.{Event, Serializer}
import today.expresso.stream.serde.utils.SpecificAvroUtils

case class PaymentMethodSuccess(id: Long, @AvroDoc("Key") @Key userId: Long, system: PaymentSystem) extends Event

object PaymentMethodSuccess extends Serializer[PaymentMethodSuccess] {
  override def toBinary(t: PaymentMethodSuccess) = SpecificAvroUtils.serialize[PaymentMethodSuccess](t)
  override def fromBinary(bytes: Array[Byte]) = SpecificAvroUtils.deserialize[PaymentMethodSuccess](bytes)

  def apply(m: PaymentMethod): PaymentMethodSuccess = PaymentMethodSuccess(m.id, m.userId, m.system)
}
