package controllers.newslet

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.AssetsFinder
import models.UserRole
import modules.DefaultEnv
import org.webjars.play.WebJarsUtil
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}
import services.RecipientService
import utils.WithRole

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class RecipientController @Inject()(
                                     silhouette: Silhouette[DefaultEnv],
                                     cc: ControllerComponents,
                                     recipientsService: RecipientService)
                                   (implicit ec: ExecutionContext,
                                    webJarsUtil: WebJarsUtil,
                                    assets: AssetsFinder)
  extends AbstractController(cc) with I18nSupport {

  import forms.newslet.RecipientForm._

  def list(newsletterId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    recipientsService.getNewsletterRecipients(newsletterId)
      .map { recipients =>
        Ok(views.html.newslet.recipient(request.identity, recipients, empty(newsletterId)))
      }
  }

  def add(newsletterId: Long, userId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    recipientsService.add(newsletterId, userId)
      .map(_ => Redirect(controllers.newslet.routes.RecipientController.list(newsletterId)))
  }

  def addForm() = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => {
        Logger.error(s"formWithErrors - s${formWithErrors}")
        Future.successful(BadRequest("TODO:"))
      },
      form => {
        recipientsService.add(form.newsletterId, form.userId.get)
          .map(_ => Redirect(controllers.newslet.routes.RecipientController.list(form.newsletterId)))
      }
    )
  }
}
