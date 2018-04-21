package services.yandex

import java.lang.invoke.MethodHandles

import javax.inject.{Inject, Singleton}
import models.Currency
import org.slf4j.LoggerFactory
import play.api.Configuration
import play.api.libs.ws.{WSClient, WSResponse}
import play.mvc.Http.Status
import services.CurrencyService

import scala.concurrent.{ExecutionContext, Future}

object YandexService {

  case class YandexServiceException(status: Int, error: Option[YandexDomain.Error]) extends Exception

  case class YandexServiceRetriableException(status: Int, error: Option[YandexDomain.Error]) extends YandexServiceException(status, error)
}

@Singleton
class YandexService @Inject()(configuration: Configuration, ws: WSClient, currencyService: CurrencyService)(implicit ec: ExecutionContext) {

  import YandexDomain._
  import YandexService._
  import today.expresso.common.utils.JsonUtils._

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  val config = configuration.get[Configuration]("gateway.yandex")

  def depositRef(userId: Long,
                 accountId: Long,
                 amount: BigDecimal,
                 currencyCode: String,
                 clientIp: String) = {

    implicit val currency: Currency = currencyService.get(currencyCode)
    val builder = new DepositWsRequestYandexBuilder(config, ws)
      .amount_=(amount)
      .clientIp_=(clientIp)
      .metadata_=(Map("userId" -> userId.toString, "accountId" -> accountId.toString))

    builder.call().map(processResponse)
  }

  private def processResponse(response: WSResponse): DepositResponse = {
    logger.info("response={}", response)
    response.status match {
      case Status.OK => response.json.validate[DepositResponse]
      case Status.ACCEPTED => throw YandexServiceException(response.status, response.json.validateOpt[YandexDomain.Error])
      case Status.BAD_REQUEST => throw YandexServiceException(response.status, response.json.validateOpt[YandexDomain.Error])
      case Status.UNAUTHORIZED => throw YandexServiceException(response.status, response.json.validateOpt[YandexDomain.Error])
      case Status.FORBIDDEN => throw YandexServiceException(response.status, response.json.validateOpt[YandexDomain.Error])
      case Status.NOT_FOUND => throw YandexServiceException(response.status, response.json.validateOpt[YandexDomain.Error])
      case Status.TOO_MANY_REQUESTS => throw YandexServiceRetriableException(response.status, response.json.validateOpt[YandexDomain.Error])
      case Status.INTERNAL_SERVER_ERROR => throw YandexServiceRetriableException(response.status, response.json.validateOpt[YandexDomain.Error])
      case _ =>
        logger.error("unknown response status")
        throw YandexServiceException(response.status, None)
    }
  }
}
