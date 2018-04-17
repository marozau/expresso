package today.expresso.stream.domain.cmd.payment

import com.sksamuel.avro4s.AvroDoc
import today.expresso.stream.api.Key
import today.expresso.stream.domain.Command

/**
  * @author im.
  */
case class DepositCommand(@AvroDoc("key") @Key userId: Long,
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