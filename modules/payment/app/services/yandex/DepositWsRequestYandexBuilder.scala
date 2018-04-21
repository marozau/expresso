package services.yandex

import java.util.UUID

import models.Currency
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.{WSAuthScheme, WSClient, WSResponse}
import services.yandex.YandexDomain.Amount

import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode

class DepositWsRequestYandexBuilder(config: Configuration, ws: WSClient)(implicit ec: ExecutionContext) {

  private val endpoint = config.get[String]("endpoint")
  private val shopId = config.get[String]("shopId")
  private val secretKey = config.get[String]("secretKey")
  private val returnUrl = config.get[String]("returnUrl")

  private val request = ws.url(endpoint)
    .withHttpHeaders("Idempotence-Key" -> UUID.randomUUID().toString)
    .withHttpHeaders("content-type" -> "application/json")
    .withAuth(shopId, secretKey, WSAuthScheme.BASIC)

  private var _amount: Amount = _
  private var _description: Option[String] = None
  private var _clientIp: Option[String] = None
  private var _paymentMethodId: Option[String] = None
  private var _save_payment_method = true
  private var _metadata: Option[Map[String, String]] = None

  def amount_=(amount: BigDecimal)(implicit currency: Currency) = {
    _amount = Amount.create(amount)
    this
  }

  def description_=(description: String) = {
    _description = Some(description)
    this
  }

  def clientIp_=(clientIp: String) = {
    _clientIp = Some(clientIp)
    this
  }

  def metadata_=(metadata: Map[String, String]) = {
    _metadata = Some(metadata)
    this
  }

  def paymentMethodId_=(paymentMethodId: String) = {
    _paymentMethodId = Some(paymentMethodId)
    _save_payment_method = false
    this
  }

  def call(): Future[WSResponse] = {
    import YandexDomain._

    val depositRequest = DepositRequest(
      _amount,
      _description,
      None,
      None,
      None,
      _paymentMethodId,
      Some(Confirmation(ConfirmationType.redirect.toString, None, Some(returnUrl), None)),
      Some(_save_payment_method),
      Some(true),
      _clientIp,
      _metadata
    )

    //    val data = Json.obj(
    //      "amount" -> Json.obj(
    //        "value" -> _amount.toString(),
    //        "currency" -> _currency
    //      ),
    //      "confirmation" -> Json.obj(
    //        "type" -> "redirect",
    //        "return_url" -> returnUrl
    //      ),
    //      "metadata" -> Json.toJson(_metadata),
    //      "payment_method_id" -> _paymentMethodId,
    //      "save_payment_method" -> _save_payment_method,
    //      "capture" -> "true",
    //      "client_ip" -> _clientIp
    //    )
    request.post(Json.toJson(depositRequest))
  }

}
