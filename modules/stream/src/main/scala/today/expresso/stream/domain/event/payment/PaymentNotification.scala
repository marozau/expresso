package today.expresso.stream.domain.event.payment

import com.sksamuel.avro4s.AvroDoc
import play.api.libs.json.JsValue
import today.expresso.grpc.payment.domain.{PaymentStatus, PaymentSystem, PaymentType}
import today.expresso.stream.api.Key
import today.expresso.stream.domain.Event

case class PaymentNotification(externalTxId: String,
                               paymentSystem: PaymentSystem,
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
                               details: Option[JsValue])
  extends Event
