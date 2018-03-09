package services.auth

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.LoginEvent
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import today.expresso.common.exceptions.{InternalServerError, UserNotFoundException, UserUnverifiedException}
import models.{ApplicationContext, User}
import play.api.Logger
import play.api.mvc.RequestHeader
import services.ServiceRegistry
import today.expresso.grpc.user.dto.UserDto
import today.expresso.common.utils.HashUtils

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class SignInService @Inject()(app: ApplicationContext,
                              registry: ServiceRegistry)
                             (implicit ec: ExecutionContext) {

  def signIn(email: String, password: String, remember: Boolean)(implicit request: RequestHeader): Future[(User, String)] = {
    val credentials = Credentials(HashUtils.encode(email), password)
    app.auth.credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
      app.auth.silhouette.env.identityService.retrieve(loginInfo).flatMap {
        case Some(user) if user.status == UserDto.Status.NEW =>
          Future.failed(UserUnverifiedException("verify your email first"))
        case Some(user) =>
          val c = app.config.underlying
          app.auth.silhouette.env.authenticatorService.create(loginInfo)
            .map {
              case authenticator if remember =>
                import net.ceedubs.ficus.Ficus._
                import scala.concurrent.duration._
                import com.mohiva.play.silhouette.api.Authenticator.Implicits._
                authenticator.copy(
                  expirationDateTime = app.clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
                  idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout"),
                )
              case authenticator => authenticator
            }
            .flatMap { authenticator =>
              app.auth.silhouette.env.eventBus.publish(LoginEvent(user, request))
              app.auth.silhouette.env.authenticatorService.init(authenticator)
                .map { token =>
                  (user, token)
                }
            }
        case None =>
          Future.failed(UserNotFoundException(s"Couldn't find user, email=$email"))
      }
    }.recover {
      case e: ProviderException =>
        Logger.error(s"Credentials provider error, message=${e.getMessage}", e)
        throw InternalServerError(e.getMessage)
    }
  }
}
