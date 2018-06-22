package controllers.dto

import today.expresso.stream.domain.model.payment.PaymentOption.PaymentOption

final case class DepositRequest(userId: Long,
                                 accountId: Long,
                                 amount: BigDecimal,
                                 commission: BigDecimal,
                                 currency: String,
                                 paymentOption: PaymentOption,
                                 ip: String)
