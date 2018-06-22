package gateways

import controllers.dto.{DepositRequest, WithdrawalRequest}
import javax.inject.{Inject, Named, Singleton}
import services.{PaymentService, PaymentSystemNames, UserProfileService}
import today.expresso.stream.domain.model.payment.{PaymentOption, PaymentSystem}
import today.expresso.stream.domain.model.payment.PaymentOption.PaymentOption
import today.expresso.stream.domain.model.payment.PaymentSystem.PaymentSystem

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class PaymentGatewayServiceImpl @Inject()(userProfileService: UserProfileService,
                                          @Named(PaymentSystemNames.YANDEX) paymentServiceYandex: PaymentService)
                                         (implicit ec: ExecutionContext)
  extends PaymentGatewayService {

  val depositDispatcher = Map[PaymentOption, PaymentService](
    PaymentOption.BANK_CARD -> paymentServiceYandex
  )

  val withdrawalDispatcher = Map[PaymentSystem, PaymentService](
    PaymentSystem.YANDEX -> paymentServiceYandex
  )

  override def deposit(request: DepositRequest) = {
    depositDispatcher(request.paymentOption).deposit(request)
  }

  override def withdrawal(request: WithdrawalRequest) = {
    withdrawalDispatcher(request.paymentSystem).withdrawal(request)
  }
}
