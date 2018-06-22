package gateways

import controllers.dto.{DepositRequest, DepositResponse, WithdrawalRequest, WithdrawalResponse}

import scala.concurrent.Future

trait PaymentGatewayService {
  def deposit(request: DepositRequest): Future[DepositResponse]
  def withdrawal(request: WithdrawalRequest): Future[WithdrawalResponse]
}
