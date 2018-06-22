package today.expresso.stream.domain.event.payment

import com.sksamuel.avro4s.AvroDoc
import today.expresso.stream.api.Key
import today.expresso.stream.domain.model.payment.PaymentMethod
import today.expresso.stream.domain.{Event, Serializer}
import today.expresso.stream.serde.utils.SpecificAvroUtils

case class PaymentMethodUpdated(@AvroDoc("Key") @Key userId: Long, method: PaymentMethod) extends Event

object PaymentMethodUpdated extends Serializer[PaymentMethodUpdated] {
  //TODO: make this global
  import today.expresso.stream.avro.JsValueMapping._
  override def toBinary(t: PaymentMethodUpdated) = SpecificAvroUtils.serialize[PaymentMethodUpdated](t)
  override def fromBinary(bytes: Array[Byte]) = SpecificAvroUtils.deserialize[PaymentMethodUpdated](bytes)

  def apply(m: PaymentMethod): PaymentMethodUpdated = PaymentMethodUpdated(m.userId, m)
}