package today.expresso.stream.domain.command.payment

import com.sksamuel.avro4s.AvroDoc
import play.api.libs.json.JsValue
import today.expresso.stream.api.Key
import today.expresso.stream.domain.{Command, Serializer}
import today.expresso.stream.serde.utils.SpecificAvroUtils

/**
  * @author im.
  */
case class Withdraw(@AvroDoc("key") @Key userId: Long,
                    accountId: Long,
                    amount: BigDecimal,
                    currency: String,
                    externalTxId: String,
                    externalSystem: String,
                    comment: String,
                    ip: String,
                    modifiedBy: String,
                    paymentMethodId: Long,
                    status: String,
                    timestamp: Long,
                    details: Option[String])
  extends Command

object Withdraw extends Serializer[Withdraw] {
  override def toBinary(t: Withdraw) = SpecificAvroUtils.serialize[Withdraw](t)
  override def fromBinary(bytes: Array[Byte]) = SpecificAvroUtils.deserialize[Withdraw](bytes)
}
