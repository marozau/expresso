package controllers.newslet

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.AssetsFinder
import models.UserRole
import modules.DefaultEnv
import org.webjars.play.WebJarsUtil
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}
import services.RecipientService
import utils.WithRole

import scala.concurrent.ExecutionContext

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

  def list(newsletterId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    recipientsService.getNewsletterRecipients(newsletterId)
      .map { recipients =>
        Ok(views.html.newslet.recipient(request.identity, recipients))
      }
  }

  //  def add(userId: Long, ) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
  //
  //  }
}
