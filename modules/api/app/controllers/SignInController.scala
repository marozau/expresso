package controllers

import java.util.concurrent.ThreadLocalRandom
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.{LoginEvent, Silhouette}
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.{Clock, Credentials}
import com.mohiva.play.silhouette.impl.providers._
import modules.DefaultEnv
import net.ceedubs.ficus.Ficus._
import org.webjars.play.WebJarsUtil
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.{Configuration, Logger}
import services.UserIdentityService
import today.expresso.grpc.Header
import today.expresso.grpc.user.dto.UserDto
import today.expresso.grpc.user.service.UserCreateRequest
import today.expresso.grpc.user.service.UserServiceGrpc.UserServiceStub
import utils.HashUtils

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * The `Sign In` controller.
  *
  * @param components          The Play controller components.
  * @param silhouette          The Silhouette stack.
  * @param userIdentityService The user service implementation.
  * @param credentialsProvider The credentials provider.
  * @param configuration       The Play configuration.
  * @param clock               The clock instance.
  * @param webJarsUtil         The webjar util.
  * @param assets              The Play assets finder.
  */
@Singleton
class SignInController @Inject()(
                                  components: ControllerComponents,
                                  silhouette: Silhouette[DefaultEnv],
                                  userIdentityService: UserIdentityService,
                                  credentialsProvider: CredentialsProvider,
                                  configuration: Configuration,
                                  clock: Clock,
                                  userServiceStub: UserServiceStub,
                                )(
                                  implicit
                                  webJarsUtil: WebJarsUtil,
                                  assets: AssetsFinder,
                                  ex: ExecutionContext
                                ) extends AbstractController(components) with I18nSupport {

  /**
    * Views the `Sign In` page.
    *
    * @return The result to display.
    */

  /**
    * Handles the submitted form.
    *
    * @return The result to display.
    */
  def submit(email: String, password: String) = silhouette.UnsecuredAction.async { implicit request =>
    val credentials = Credentials(HashUtils.encode(email), password)
    credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
      userIdentityService.retrieve(loginInfo).flatMap {
        case Some(user) if user.status == UserDto.Status.NEW =>
          Future.successful(Ok)
        case Some(user) =>
          val c = configuration.underlying
          silhouette.env.authenticatorService.create(loginInfo)
            .map {
              case authenticator if true =>
                authenticator.copy(
                  expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
                  idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout"),
                )
              case authenticator => authenticator
            }
            .flatMap { authenticator =>
              silhouette.env.eventBus.publish(LoginEvent(user, request))
              silhouette.env.authenticatorService.init(authenticator)
                .map { token =>
                  Ok(Json.obj("token" -> token))
                }
            }
        case None =>
          Logger.error(s"Couldn't find user, email=${email}")
          Future.successful(InternalServerError)
      }
    }.recover {
      case e: ProviderException =>
        Logger.error(s"Credentials provider error, message=${e.getMessage}", e)
        InternalServerError
    }
  }

  def signUp(email: String, password: String) = Action.async { implicit rq =>
    userServiceStub.userCreate(
      UserCreateRequest(
        Some(Header(ThreadLocalRandom.current().nextInt())),
        email,
        password
      )
    )
      .map(_.user)
      .map(user => Ok(Json.obj("user" -> user.toString)))
  }
}
