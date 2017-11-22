package controllers.site

import javax.inject.{Inject, Singleton}

import controllers.AssetsFinder
import models.Recipient
import org.webjars.play.WebJarsUtil
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{AuthTokenService, RecipientService, UserService}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class SubscriptionController @Inject()(
                                        cc: ControllerComponents,
                                        recipientsService: RecipientService,
                                        authTokenService: AuthTokenService,
                                        userService: UserService)
                                      (implicit ec: ExecutionContext,
                                       webJarsUtil: WebJarsUtil,
                                       assets: AssetsFinder)
  extends AbstractController(cc) with I18nSupport {

  import forms.site.SignUpForm._

  def signUpForm(newsletterId: Long) = Action { implicit request =>
    Ok(views.html.site.subscribe(empty(newsletterId)))
  }

  // TODO: store cache with verified user id after first subscription verification and don't send double opt in after that
  def submitSignUpForm() = Action.async { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => {
        Logger.error(s"formWithErrors - s$formWithErrors")
        Future.successful(BadRequest("TODO:"))
      },
      form => {
        recipientsService.subscribe(form.newsletterId, form.email.get, Recipient.Status.PENDING)
          .map { recipient =>
            Ok("")
          }
      }
    )
  }
}
