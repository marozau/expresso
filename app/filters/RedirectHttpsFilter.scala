package filters

import java.net.InetAddress
import javax.inject.{Inject, Singleton}

import play.api.Environment
import play.api.http.HeaderNames._
import play.api.http.Status._
import play.api.mvc.{EssentialAction, EssentialFilter, RequestHeader, Results}

import scala.concurrent.ExecutionContext

/**
  * based on https://www.playframework.com/documentation/latest/RedirectHttpsFilter
  *
  * GCP Load Balancer adds 2 x-forwarded-for addresses and only 1 x-forwarded-proto (https)
  * which is interpreted by play framework as unsecure.
  * At first we check if loaded balancer forward secure connection and then manually set secure flat to true
  * All inter cluster communications must be unsecure and add as exceptions using healthCheckUri (it worth to make it configurable)
  * @author im.
  */
@Singleton
class RedirectHttpsFilter @Inject()(env: Environment)(implicit ec: ExecutionContext) extends EssentialFilter {

  lazy val redirectEnabled = env.mode == play.api.Mode.Prod

  lazy val stsHeaders = Seq(STRICT_TRANSPORT_SECURITY -> "max-age=31536000; includeSubDomains")
  lazy val healthCheckUri = "/healthz" //TODO: move to config, possible it's better

  override def apply(next: EssentialAction): EssentialAction = EssentialAction { req =>
    import play.api.libs.streams.Accumulator
    val secure = req.headers.get(X_FORWARDED_PROTO).exists(_.startsWith("https"))
    if (secure) {
      next(makeSecure(req)).map(_.withHeaders(stsHeaders: _*))
    } else if (redirectEnabled && req.uri != healthCheckUri) {
      Accumulator.done(Results.Redirect(createHttpsRedirectUrl(req), PERMANENT_REDIRECT))
    } else {
      next(makeSecure(req))
    }
  }

  protected def createHttpsRedirectUrl(req: RequestHeader): String = {
    import req.{domain, uri}
    s"https://$domain$uri"
  }

  protected def makeSecure(request: RequestHeader) = {
    import play.api.mvc.request.RemoteConnection
    request.withConnection(
      new RemoteConnection {
        override def remoteAddress: InetAddress = request.connection.remoteAddress
        override def secure: Boolean = redirectEnabled
        override lazy val clientCertificateChain = request.connection.clientCertificateChain
      }
    )
  }
}
