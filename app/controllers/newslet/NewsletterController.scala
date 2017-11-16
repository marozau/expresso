package controllers.newslet

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.AssetsFinder
import models.{Newsletter, UserRole}
import modules.DefaultEnv
import org.webjars.play.WebJarsUtil
import play.api.Logger
import play.api.i18n.{I18nSupport, Lang, Langs}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.NewsletterService
import utils.WithRole

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class NewsletterController @Inject()(silhouette: Silhouette[DefaultEnv],
                                     cc: ControllerComponents,
                                     newsletterService: NewsletterService)
                                    (implicit
                                     ec: ExecutionContext,
                                     webJarsUtil: WebJarsUtil,
                                     assets: AssetsFinder,
                                     langs: Langs)
  extends AbstractController(cc) with I18nSupport {

  import forms.newslet.NewsletterForm._

  def getList() = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    newsletterService.list()
      .map { newsletters =>
        Ok(views.html.newslet.newsletterList(request.identity, form, newsletters))
      }
  }

  def create() = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => {
        Logger.error(formWithErrors.toString)
        newsletterService.list()
          .map { newsletters =>
            BadRequest(views.html.newslet.newsletterList(request.identity, formWithErrors, newsletters))
          }
      },
      form => {
        val newsletter = Newsletter(None, request.identity.id.get, form.name, form.nameUrl, form.email, Lang(form.lang), form.logo)
        newsletterService.create(newsletter)
          .map(_ => Redirect(controllers.newslet.routes.NewsletterController.getList()))
      }
    )
  }
}
