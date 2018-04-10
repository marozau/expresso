package gateways

import javax.inject.{Inject, Named, Singleton}
import models.CompositeUserInfo
import services.{PaymentService, PaymentSystemNames}
import today.expresso.common.grpc.GrpcErrorHandler
import today.expresso.grpc.payment.domain.{PaymentOption, PaymentSystem}
import today.expresso.grpc.payment.service.{DepositRequest, PaymentGatewayServiceGrpc, WithdwawalRequest}

/**
  * @author im.
  */
@Singleton
class PaymentGatewayServiceImpl @Inject()(
                                    @Named(PaymentSystemNames.YANDEX) paymentServiceYandex: PaymentService
                                  )
                                         (implicit ec: Exception)
  extends PaymentGatewayServiceGrpc.PaymentGatewayService {

  val depositDispatcher = Map[PaymentOption, (CompositeUserInfo) => PaymentService] (
    PaymentOption.BANK_CARD -> ((_: CompositeUserInfo) => paymentServiceYandex)
  )
  
  val withdrawalDispatcher = Map[PaymentSystem, PaymentService] (
    PaymentSystem.YANDEX -> paymentServiceYandex
  )

  override def deposit(request: DepositRequest) = GrpcErrorHandler {
    depositDispatcher(request.paymentOption)(CompositeUserInfo()).deposit(request)
  }

  override def withdrawal(request: WithdwawalRequest) = GrpcErrorHandler {
    withdrawalDispatcher(request.paymentSystem).withdrawal(request)
  }
}
