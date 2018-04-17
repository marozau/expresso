package today.expresso.stream.domain.event.payment

import com.sksamuel.avro4s.AvroDoc
import today.expresso.stream.api.Key
import today.expresso.stream.domain.Event

case class PaymentMethodSuccess(id: Long, @AvroDoc("Key") @Key userId: Long) extends Event
