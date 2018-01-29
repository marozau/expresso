package filters

import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import play.api.Logger
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author im.
  */
@Singleton
class AccessLoggingFilter @Inject()(implicit val mat: Materializer) extends Filter {

  val accessLogger = Logger("access")

  def apply(next: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {
    val resultFuture = next(request)

    resultFuture.foreach(result => {
      val msg = s"method=${request.method} uri=${request.uri} remote-address=${request.remoteAddress} headers=${request.headers}" +
        s" status=${result.header.status}"
      accessLogger.info(msg)
      //TODO: send all error responses to analytics
    })

    resultFuture
  }
}
