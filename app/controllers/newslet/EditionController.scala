package controllers.newslet

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

import clients.PublishingHouse
import clients.PublishingHouse.Target
import com.mohiva.play.silhouette.api.Silhouette
import controllers.AssetsFinder
import forms.newslet.PostForm.{Data, form}
import models.UserRole
import models.daos.PostDao
import modules.DefaultEnv
import org.webjars.play.WebJarsUtil
import play.api.Logger
import play.api.cache.AsyncCacheApi
import play.api.i18n.{I18nSupport, Messages, MessagesImpl}
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
                                 )(implicit
                                   ec: ExecutionContext,
                                   webJarsUtil: WebJarsUtil,
                                   assets: AssetsFinder)
  extends AbstractController(cc) with I18nSupport {

  import forms.newslet.EditionForm._
  import implicits.CommonImplicits._


  def list(newsletterId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    (for {
      newsletter <- newsletterService.getById(newsletterId)
      editions <- editionService.list(newsletterId)
    } yield (newsletter, editions))
      .map { case (newsletter, edition) =>
        Ok(views.html.newslet.editions(request.identity, newsletter, edition))
      }
  }

  def create(newsletterId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    editionService.create(newsletterId)
      .flatMap(edition => ph.doEdition(edition, Target.SITE))
      .map { edition =>
        Ok(views.html.newslet.newsletterPosts(edition))
      }
  }

  def get(editionId: Long, cleanCache: Boolean) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    editionService.getById(editionId)
      .flatMap { edition =>
        if (cleanCache) {
          Logger.info(s"cleaning cache, editionId=$editionId")
          for {
            _ <- cache.remove(s"/archive/${edition.newsletter.nameUrl}/${edition.date.format(DateTimeFormatter.BASIC_ISO_DATE)}")
            _ <- cache.remove(s"/archive/${edition.newsletter.nameUrl}/${edition.date.format(DateTimeFormatter.BASIC_ISO_DATE)}-etag")
            _ <- Future.sequence(
              edition.posts.map(post => cache.remove(s"/archive/${edition.newsletter.nameUrl}/${edition.date.format(DateTimeFormatter.BASIC_ISO_DATE)}/${post.titleUrl}")))
            _ <- Future.sequence(
              edition.posts.map(post => cache.remove(s"/archive/${edition.newsletter.nameUrl}/${edition.date.format(DateTimeFormatter.BASIC_ISO_DATE)}/${post.titleUrl}-etag")))
          } yield edition
        } else Future.successful(edition)
      }
      .flatMap(edition => ph.doEdition(edition, Target.SITE))
      .map { edition =>
        Ok(views.html.newslet.newsletterPosts(edition))
      }
  }

  def addPost(id: Long, postId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    editionService.addPost(id, List(postId))
      .map(_ => Redirect(controllers.newslet.routes.EditionController.get(id, cleanCache = true)))
  }

  def removePost(id: Long, postId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    editionService.removePost(id, postId)
      .map(_ => Redirect(controllers.newslet.routes.EditionController.get(id, cleanCache = true)))
  }

  def moveUpPost(id: Long, postId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    editionService.moveUpPost(id, postId)
      .map(_ => Redirect(controllers.newslet.routes.EditionController.get(id, cleanCache = true)))
  }

  def moveDownPost(id: Long, postId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    editionService.moveDownPost(id, postId)
      .map(_ => Redirect(controllers.newslet.routes.EditionController.get(id, cleanCache = true)))
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

  def getPostForm(id: Option[Long], editionId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    def getExisting(id: Long) = {
      posts.getById(id).map(p => form.fill(p))
    }

    def create(newsletterId: Long) = {
      Future(form.fill(Data(None, Some(newsletterId), "", "", "", List.empty)))
    }

    editionService.getById(editionId)
      .flatMap(edition => ph.doEdition(edition, Target.SITE).map((edition, _)))
      .flatMap { case (edition, newsletter) => id.fold(create(editionId))(getExisting).map((edition, newsletter, _)) }
      .map { case (_, newsletter, postForm) =>
        Ok(views.html.newslet.newsletterPosts(newsletter, postForm = Some(postForm)))
      }
  }

  def getHeaderForm(editionId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    editionService.getById(editionId)
      .flatMap(edition => ph.doEdition(edition, Target.SITE).map((edition, _)))
      .map { case (edition, newsletter) =>
        Ok(views.html.newslet.newsletterPosts(newsletter, headerForm = Some(headerForm.fill(HeaderData(edition.id.get, edition.header)))))
      }
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
          .map(_ => Redirect(routes.EditionController.get(form.id, cleanCache = true)))
      }
    )
  }

  def getFooterForm(editionId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    editionService.getById(editionId)
      .flatMap(edition => ph.doEdition(edition, Target.SITE).map((edition, _)))
      .map { case (edition, newsletter) =>
        Ok(views.html.newslet.newsletterPosts(newsletter, footerForm = Some(footerForm.fill(FooterData(edition.id.get, edition.footer)))))
      }
  }

  def submitFooterForm() = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    footerForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.info(s"bad newsletter footer, form=$formWithErrors")
        Future.successful(BadRequest(views.html.newslet.footer(formWithErrors)))
      },
      form => {
        editionService.getById(form.id)
          .flatMap { edition =>
            editionService.update(edition.copy(footer = Some(form.text)))
          }
          .map(_ => Redirect(routes.EditionController.get(form.id, cleanCache = true)))
      }
    )
  }

  def getTitleForm(editionId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    editionService.getById(editionId)
      .flatMap(edition => ph.doEdition(edition, Target.SITE))
      .map { edition =>
        Ok(views.html.newslet.newsletterPosts(edition, titleForm = Some(titleForm.fill(TitleData(edition.id.get, edition.title)))))
      }
  }

  def submitTitleForm() = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    titleForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.info(s"bad newsletter title, form=$formWithErrors")
        Future.successful(BadRequest(views.html.newslet.title(formWithErrors)))
      },
      form => {
        editionService.getById(form.id)
          .flatMap { edition =>
            editionService.update(edition.copy(title = Some(form.text)))
          }
          .map(_ => Redirect(routes.EditionController.get(form.id, cleanCache = true)))
      }
    )
  }

  def getDateForm(editionId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    editionService.getById(editionId)
      .flatMap(edition => ph.doEdition(edition, Target.SITE))
      .map { edition =>
        Ok(views.html.newslet.newsletterPosts(edition, dateForm = Some(dateForm.fill(DateData(edition.id.get, edition.date)))))
      }
  }

  def submitDateForm() = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    dateForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.info(s"bad newsletter date, form=$formWithErrors")
        Future.successful(Redirect(routes.EditionController.get(formWithErrors.data("id").toLong, cleanCache = false)))
      },
      form => {
        editionService.getById(form.id)
          .flatMap { edition =>
            editionService.update(edition.copy(date = form.date))
          }
          .map(_ => Redirect(routes.EditionController.get(form.id, cleanCache = true)))
      }
    )
  }

  def getUrlForm(editionId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    editionService.getById(editionId)
      .flatMap(edition => ph.doEdition(edition, Target.SITE))
      .map { edition =>
        Ok(views.html.newslet.newsletterPosts(edition, urlForm = Some(urlForm.fill(UrlData(edition.id.get, edition.url)))))
      }
  }

  def submitUrlForm() = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    urlForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.info(s"bad newsletter url, form=$formWithErrors")
        Future.successful(Redirect(routes.EditionController.get(formWithErrors.data("id").toLong, cleanCache = false)))
      },
      form => {
        editionService.getById(form.id)
          .flatMap { edition =>
            editionService.update(edition.copy(url = form.url))
          }
          .map(_ => Redirect(routes.EditionController.get(form.id, cleanCache = true)))
      }
    )
  }

  def preview(id: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    editionService.getById(id)
      .flatMap(edition => ph.doEdition(edition, PublishingHouse.Target.SITE))
      .map { edition =>
        implicit val messages: Messages = MessagesImpl(edition.newsletter.lang, messagesApi)
        Ok(views.html.site.newsletter(edition)(request, messages, assets))
      }
  }

  //  def firepad() = Action { implicit request =>
  //    Ok(views.html.newslet.firepad())
  //  }
}
