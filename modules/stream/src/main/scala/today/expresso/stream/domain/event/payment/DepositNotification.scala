package today.expresso.stream.domain.event.payment

import today.expresso.stream.api.Key

/**
  * @author im.
  */
case class WithdrawalNotification(@Key userId: Long,
                                  accountId: Long,
                                  amount: BigDecimal,
                                  currency: String,
                                  externalTxId: String,
                                  externalSystem: String,
                                  comment: String,
                                  ip: String,
                                  modifiedBy: String,
                                  paymentStatus: String)
