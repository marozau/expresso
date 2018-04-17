package today.expresso.stream.domain.cmd.payment

import com.sksamuel.avro4s.AvroDoc
import play.api.libs.json.JsValue
import today.expresso.stream.api.Key
import today.expresso.stream.domain.Command

/**
  * @author im.
  */
case class WithdrawalCommand(@AvroDoc("key") @Key userId: Long,
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
                             details: Option[JsValue])
  extends Command
