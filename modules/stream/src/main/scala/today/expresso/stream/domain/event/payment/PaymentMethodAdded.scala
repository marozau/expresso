package today.expresso.stream.domain.event.payment

import com.sksamuel.avro4s.AvroDoc
import today.expresso.stream.api.Key
import today.expresso.stream.domain.model.payment.PaymentMethod
import today.expresso.stream.domain.{Event, Serializer}
import today.expresso.stream.serde.utils.SpecificAvroUtils

case class PaymentMethodAdded(@AvroDoc("Key") @Key userId: Long, method: PaymentMethod) extends Event

object PaymentMethodAdded extends Serializer[PaymentMethodAdded] {
  import today.expresso.stream.avro.JsValueMapping._
  override def toBinary(t: PaymentMethodAdded) = SpecificAvroUtils.serialize[PaymentMethodAdded](t)
  override def fromBinary(bytes: Array[Byte]) = SpecificAvroUtils.deserialize[PaymentMethodAdded](bytes)

  def apply(m: PaymentMethod): PaymentMethodAdded = PaymentMethodAdded(m.userId, m)
}