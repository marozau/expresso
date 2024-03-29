package controllers.auth

import javax.inject.Inject

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.api.{LoginInfo, SignUpEvent, Silhouette}
import com.mohiva.play.silhouette.impl.providers._
import controllers.AssetsFinder
import forms.auth.SignUpForm
import models.{User, UserRole, UserStatus}
import modules.DefaultEnv
import org.webjars.play.WebJarsUtil
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import services.{AuthTokenService, UserService}
import utils.WithRole

import scala.concurrent.{ExecutionContext, Future}

/**
  * The `Sign Up` controller.
  *
  * @param components             The Play controller components.
  * @param silhouette             The Silhouette stack.
  * @param userService            The user service implementation.
  * @param authInfoRepository     The auth info repository implementation.
  * @param authTokenService       The auth token service implementation.
  * @param passwordHasherRegistry The password hasher registry.
  * @param mailerClient           The mailer client.
  * @param webJarsUtil            The webjar util.
  * @param assets                 The Play assets finder.
  * @param ex                     The execution context.
  */
class SignUpController @Inject()(
                                  components: ControllerComponents,
                                  silhouette: Silhouette[DefaultEnv],
                                  userService: UserService,
                                  authInfoRepository: AuthInfoRepository,
                                  authTokenService: AuthTokenService,
                                  passwordHasherRegistry: PasswordHasherRegistry,
                                  mailerClient: MailerClient
                                )(
                                  implicit
                                  webJarsUtil: WebJarsUtil,
                                  assets: AssetsFinder,
                                  ex: ExecutionContext
                                ) extends AbstractController(components) with I18nSupport {

  /**
    * Views the `Sign Up` page.
    *
    * @return The result to display.
    */
  def view = silhouette.SecuredAction(WithRole(UserRole.ADMIN, UserRole.EDITOR)).async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.auth.signUp(SignUpForm.form)))
  }

  /**
    * Handles the submitted form.
    *
    * @return The result to display.
    */
  def submit = silhouette.SecuredAction(WithRole(UserRole.ADMIN, UserRole.EDITOR)).async { implicit request: Request[AnyContent] =>
    SignUpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.auth.signUp(form))),
      data => {
        val result = Redirect(routes.SignUpController.view()).flashing("info" -> Messages("sign.up.email.sent", data.email))
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        userService.retrieve(loginInfo).flatMap {
          case Some(_) =>
            Future.successful(Redirect(routes.SignUpController.view()).flashing("error" -> Messages("sign.up.email.occupied", data.email)))
          case None =>
            val authInfo = passwordHasherRegistry.current.hash(data.password)
            val user = User(
              id = None,
              loginInfo = loginInfo,
              email = data.email,
              roles = List(UserRole.WRITER),
              status = UserStatus.NEW
            )
            import scala.concurrent.duration._
            for {
              user <- userService.save(user)
              _ <- authInfoRepository.add(loginInfo, authInfo)
              authToken <- authTokenService.create(user.id.get, 1.day)
            } yield {
              val url = routes.ActivateAccountController.activate(authToken.id).absoluteURL()
              mailerClient.send(Email(
                subject = Messages("email.sign.up.subject"),
                from = Messages("email.from"),
                to = Seq(data.email),
                bodyText = Some(s"""Click <a href="$url">here</a> to send the activation email again.""")
                //                bodyText = Some(views.txt.emails.signUp(user, url).body),
                //                bodyHtml = Some(views.html.emails.signUp(user, url).body)
              ))

              silhouette.env.eventBus.publish(SignUpEvent(user, request))
              result
            }
        }
      }
    )
  }
}
