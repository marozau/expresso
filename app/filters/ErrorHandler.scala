package filters

import javax.inject._

import exceptions.PostNotFoundException
import play.api.http.DefaultHttpErrorHandler
import play.api._
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
                              router: Provider[Router]
                            ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  override def onProdServerError(request: RequestHeader, exception: UsefulException) = {
    val result = exception.cause match {
      case e: PostNotFoundException => NotFound(views.html.admin.error(e))
      case t: Throwable => InternalServerError(views.html.admin.error(t))
    }
    Future.successful(result)
  }

  override def onForbidden(request: RequestHeader, message: String) = {
    Future.successful(
      Forbidden("You're not allowed to access this resource.")
    )
  }
}