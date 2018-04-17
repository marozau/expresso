package services.yandex

import java.lang.invoke.MethodHandles

import javax.inject.Inject
import org.slf4j.LoggerFactory
import services.PaymentService
import today.expresso.grpc.payment.domain.{PaymentPageReference, PaymentSystem}
import today.expresso.grpc.payment.service._

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
class PaymentServiceYandex @Inject()(yandexService: YandexService)(implicit ec: ExecutionContext)
  extends PaymentService {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  override def paymentSystem = PaymentSystem.YANDEX

  override def deposit(request: DepositRequest) = {

    yandexService.depositRef(
      request.userId,
      request.accountId,
      request.amount,
      request.currency,
      request.ip
    ).map { response =>
      DepositResponse(
        request.header,
        Some(PaymentPageReference(response.confirmation.get.confirmationUrl.get)))
    }.recover {
      case e: Throwable =>
        logger.error("deposit request failed", e)
        throw new RuntimeException(e)
    }
  }

  override def withdrawal(request: WithdwawalRequest) = ???
}
