package gateways

import javax.inject.{Inject, Named, Singleton}
import services.{PaymentService, PaymentSystemNames, UserProfileService}
import today.expresso.common.grpc.GrpcErrorHandler
import today.expresso.grpc.payment.domain.{PaymentOption, PaymentSystem}
import today.expresso.grpc.payment.service.{DepositRequest, PaymentGatewayServiceGrpc, WithdwawalRequest}

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class PaymentGatewayServiceImpl @Inject()(userProfileService: UserProfileService,
                                          @Named(PaymentSystemNames.YANDEX) paymentServiceYandex: PaymentService)
                                         (implicit ec: ExecutionContext)
  extends PaymentGatewayServiceGrpc.PaymentGatewayService {

  val depositDispatcher = Map[PaymentOption, PaymentService](
    PaymentOption.BANK_CARD -> paymentServiceYandex
  )

  val withdrawalDispatcher = Map[PaymentSystem, PaymentService](
    PaymentSystem.YANDEX -> paymentServiceYandex
  )

  override def deposit(request: DepositRequest) = GrpcErrorHandler {
    depositDispatcher(request.paymentOption).deposit(request)
  }

  override def withdrawal(request: WithdwawalRequest) = GrpcErrorHandler {
    withdrawalDispatcher(request.paymentSystem).withdrawal(request)
  }
}
