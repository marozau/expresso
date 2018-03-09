package filters

import java.lang.invoke.MethodHandles
import javax.inject._

import controllers.AssetsFinder
import today.expresso.common.exceptions.BaseException
import org.slf4j.LoggerFactory
import play.api.http.DefaultHttpErrorHandler
import play.api.i18n.MessagesApi
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.Router
import play.api.{Configuration, Environment, OptionalSourceMapper, UsefulException}

import scala.concurrent._

/**
  * @author im.
  */

@Singleton
class ErrorHandler @Inject()(
                              env: Environment,
                              config: Configuration,
                              sourceMapper: OptionalSourceMapper,
                              router: Provider[Router],
                              messagesApi: MessagesApi,
                            )(implicit assets: AssetsFinder)
  extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  override protected def onDevServerError(request: RequestHeader, exception: UsefulException) = {
    val message = if (exception.cause.isInstanceOf[BaseException]) exception.cause.toString else exception.getMessage
    logger.error(s"request failed, request=$request, message=$message", exception)
    super.onDevServerError(request, exception)
  }

  override def onProdServerError(request: RequestHeader, exception: UsefulException) = {
    val message = if (exception.cause.isInstanceOf[BaseException]) exception.cause.toString else exception.getMessage
    logger.error(s"request failed, request=$request, message=$message", exception)
    super.onProdServerError(request, exception)
  }

  override def onForbidden(request: RequestHeader, message: String) = {
    Future.successful(
      Forbidden("You're not allowed to access this resource.")
    )
  }
}