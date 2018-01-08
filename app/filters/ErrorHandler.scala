package filters

import java.lang.invoke.MethodHandles
import javax.inject._

import controllers.AssetsFinder
import exceptions.{BaseException, EditionNotFoundException, PostNotFoundException}
import org.slf4j.LoggerFactory
import play.api.http.DefaultHttpErrorHandler
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.{Configuration, Environment, OptionalSourceMapper, UsefulException}
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc._
import play.api.mvc.Results._
import play.api.routing.Router

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
    implicit val implicitRequest: RequestHeader = request
    implicit val messages: Messages = MessagesImpl(request.acceptLanguages.headOption.getOrElse(Lang.defaultLang), messagesApi)
    val result = exception.cause match {
      case _: PostNotFoundException => NotFound(views.html.common.error(NOT_FOUND))
      case _: EditionNotFoundException => NotFound(views.html.common.error(NOT_FOUND))
      case _: Throwable => InternalServerError(views.html.common.error(INTERNAL_SERVER_ERROR))
    }
    Future.successful(result)
  }

  override def onForbidden(request: RequestHeader, message: String) = {
    Future.successful(
      Forbidden("You're not allowed to access this resource.")
    )
  }
}