package controllers.dto

import today.expresso.stream.domain.model.payment.PaymentSystem.PaymentSystem

case class WithdrawalRequest(paymentSystem: PaymentSystem)
