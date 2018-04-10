package services

import javax.inject.Inject
import today.expresso.grpc.payment.domain.PaymentSystem
import today.expresso.grpc.payment.service._

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
class PaymentServiceYandex @Inject()(implicit ec: ExecutionContext)
  extends PaymentService {

  override def paymentSystem = PaymentSystem.YANDEX

  override def deposit(request: DepositRequest) = ???

  override def withdrawal(request: WithdwawalRequest) = ???
}
