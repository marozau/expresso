package controllers.auth

import java.net.URLDecoder
import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.UserStatus
import modules.DefaultEnv
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import services.{AuthTokenService, UserService}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * The `Activate Account` controller.
  *
  * @param components       The Play controller components.
  * @param silhouette       The Silhouette stack.
  * @param userService      The user service implementation.
  * @param authTokenService The auth token service implementation.
  * @param mailerClient     The mailer client.
  * @param ex               The execution context.
  */
class ActivateAccountController @Inject()(
                                           components: ControllerComponents,
                                           silhouette: Silhouette[DefaultEnv],
                                           userService: UserService,
                                           authTokenService: AuthTokenService,
                                           mailerClient: MailerClient
                                         )(
                                           implicit
                                           ex: ExecutionContext
                                         ) extends AbstractController(components) with I18nSupport {

  /**
    * Sends an account activation email to the user with the given email.
    *
    * @param email The email address of the user to send the activation mail to.
    * @return The result to display.
    */
  def send(email: String) = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    val decodedEmail = URLDecoder.decode(email, "UTF-8")
    val loginInfo = LoginInfo(CredentialsProvider.ID, decodedEmail)
    val result = Redirect(routes.SignInController.view()).flashing("info" -> Messages("activation.email.sent", decodedEmail))

    userService.retrieve(loginInfo).flatMap {
      case Some(user) if user.status == UserStatus.NEW =>
        authTokenService.create(user.id.get, 1.day).map { authToken =>
          val url = routes.ActivateAccountController.activate(authToken.id).absoluteURL()

          mailerClient.send(Email(
            subject = Messages("email.activate.account.subject"),
            from = Messages("email.from"),
            to = Seq(decodedEmail),
            bodyText = Some(s"""Click <a href="$url">here</a> to send the activation email again.""")
            //            bodyHtml = Some(views.html.emails.activateAccount(user, url).body)
          ))
          result
        }
      case None => Future.successful(result)
    }
  }

  /**
    * Activates an account.
    *
    * @param token The token to identify a user.
    * @return The result to display.
    */
  def activate(token: UUID) = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    authTokenService.validate(token).flatMap {
      case Some(authToken) => userService.retrieve(authToken.userId).flatMap {
        case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
          userService.save(user.copy(status = UserStatus.VERIFIED)).map { _ =>
            Redirect(routes.SignInController.view()).flashing("success" -> Messages("account.activated"))
          }
        case _ => Future.successful(Redirect(routes.SignInController.view()).flashing("error" -> Messages("invalid.activation.link")))
      }
      case None => Future.successful(Redirect(routes.SignInController.view()).flashing("error" -> Messages("invalid.activation.link")))
    }
  }
}
