package controllers.newslet

import javax.inject.{Inject, Singleton}

import clients.PublishingHouse
import clients.PublishingHouse.Target
import com.mohiva.play.silhouette.api.Silhouette
import models.UserRole
import models.daos.PostDao
import modules.DefaultEnv
import play.api.Logger
import play.api.cache.AsyncCacheApi
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{EditionService, NewsletterService}
import utils.{HtmlUtils, WithRole}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */

@Singleton
class EditionController @Inject()(
                                   silhouette: Silhouette[DefaultEnv],
                                   cache: AsyncCacheApi,
                                   cc: ControllerComponents,
                                   editionService: EditionService,
                                   newsletterService: NewsletterService,
                                   posts: PostDao,
                                   htmlUtils: HtmlUtils,
                                   ph: PublishingHouse
                                 )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  import forms.newslet.NewsletterForm._
  import implicits.CommonImplicits._

  def getNewsletterList() = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    val userId = request.identity.id.get
    newsletterService.list()
      .map { newsletters =>
        Ok(views.html.newslet.newsletterList(newsletters))
      }
  }

  def getOrCreate(newsletterId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    editionService.getUnpublishedOrCreate(newsletterId)
      .flatMap(edition => ph.doEdition(edition, Target.SITE))
      .map { edition =>
        Ok(views.html.newslet.newsletterPosts(edition))
      }
  }

  def get(editionId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    editionService.getById(editionId)
      .flatMap(edition => ph.doEdition(edition, Target.SITE))
      .map { edition =>
        Ok(views.html.newslet.newsletterPosts(edition))
      }
  }

  def addPost(id: Long, postId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    editionService.addPost(id, List(postId))
      .map(_ => Redirect(controllers.newslet.routes.EditionController.get(id)))
  }

  def removePost(id: Long, postId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    editionService.removePost(id, postId)
      .map(_ => Redirect(controllers.newslet.routes.EditionController.get(id)))
  }

  def moveUpPost(id: Long, postId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    editionService.moveUpPost(id, postId)
      .map(_ => Redirect(controllers.newslet.routes.EditionController.get(id)))
  }

  def moveDownPost(id: Long, postId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    editionService.moveDownPost(id, postId)
      .map(_ => Redirect(controllers.newslet.routes.EditionController.get(id)))
  }

  //TODO: replace by ususal get newsletter
  def getNewsletterFinal(campaignId: Long, editionId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    editionService.getById(editionId)
      .flatMap { edition =>
        ph.doEdition(edition, PublishingHouse.Target.SITE)
      }
      .map { edition =>
        Ok(views.html.newslet.newsletterFinal(edition, campaignId))
      }
  }

  def getHeaderForm(editionId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    editionService.getById(editionId)
      .map(nl => Ok(views.html.newslet.header(headerForm.fill(HeaderData(nl.id.get, nl.header)))))
  }

  def submitHeaderForm() = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    headerForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.info(s"bad newsletter header, form=$formWithErrors")
        Future(BadRequest(views.html.newslet.header(formWithErrors)))
      },
      form => {
        editionService.getById(form.id)
          .flatMap { edition =>
            editionService.update(edition.copy(header = Some(form.text)))
          }
          .map(_ => Redirect(routes.EditionController.get(form.id)))
      }
    )
  }

  def getFooterForm(editionId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    editionService.getById(editionId)
      .map(nl => Ok(views.html.newslet.footer(footerForm.fill(FooterData(nl.id.get, nl.footer)))))
  }

  def submitFooterForm() = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    val userId = request.identity.id.get
    footerForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.info(s"bad newsletter footer, form=$formWithErrors")
        Future(BadRequest(views.html.newslet.footer(formWithErrors)))
      },
      form => {
        editionService.getById(form.id)
          .flatMap { edition =>
            editionService.update(edition.copy(footer = Some(form.text)))
          }
          .map(_ => Redirect(routes.EditionController.get(form.id)))
      }
    )
  }

  def firepad() = Action { implicit request =>
    Ok(views.html.newslet.firepad())
  }
}
