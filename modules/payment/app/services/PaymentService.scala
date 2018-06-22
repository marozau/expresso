package services

import gateways.PaymentGatewayService
import today.expresso.stream.domain.model.payment.PaymentSystem.PaymentSystem

/**
  * @author im.
  */
trait PaymentService extends PaymentGatewayService {

  def paymentSystem: PaymentSystem

}
