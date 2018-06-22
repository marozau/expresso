package today.expresso.stream.domain.command.payment

import com.sksamuel.avro4s.AvroDoc
import today.expresso.stream.api.Key
import today.expresso.stream.domain.{Command, Serializer}
import today.expresso.stream.serde.utils.SpecificAvroUtils

/**
  * @author im.
  */
case class Deposit(@AvroDoc("key") @Key userId: Long,
                   accountId: Long,
                   amount: BigDecimal,
                   currency: String,
                   externalTxId: String,
                   externalSystem: String,
                   comment: String,
                   ip: String,
                   modifiedBy: String,
                   paymentStatus: String)
  extends Command


object Deposit extends Serializer[Deposit] {
  override def toBinary(t: Deposit) = SpecificAvroUtils.serialize[Deposit](t)
  override def fromBinary(bytes: Array[Byte]) = SpecificAvroUtils.deserialize[Deposit](bytes)
}