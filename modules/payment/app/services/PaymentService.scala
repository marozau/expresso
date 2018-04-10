package services

import today.expresso.grpc.payment.domain.PaymentSystem
import today.expresso.grpc.payment.service.PaymentGatewayServiceGrpc.PaymentGatewayService

/**
  * @author im.
  */
trait PaymentService extends PaymentGatewayService {

  def paymentSystem: PaymentSystem

}
