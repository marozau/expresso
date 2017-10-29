package controllers.office

import javax.inject.{Inject, Singleton}

import clients.PublishingHouse
import clients.PublishingHouse.Target
import com.mohiva.play.silhouette.api.Silhouette
import models.{NewsletterAndPosts, UserRole}
import models.daos.{NewsletterDao, PostDao}
import modules.DefaultEnv
import play.api.Logger
import play.api.cache.AsyncCacheApi
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.{HtmlUtils, WithRole}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */

@Singleton
class NewsletterController @Inject()(
                                      silhouette: Silhouette[DefaultEnv],
                                      cache: AsyncCacheApi,
                                      cc: ControllerComponents,
                                      newsletters: NewsletterDao,
                                      posts: PostDao,
                                      htmlUtils: HtmlUtils,
                                      ph: PublishingHouse
                                    )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  import forms.office.NewsletterForm._
  import implicits.NewsletterImplicits._

  def getNewsletterList() = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    val userId = request.identity.id.get
    newsletters.getByUserId(userId)
      .map { newsletters =>
        Ok(views.html.office.newsletterList(newsletters))
      }
  }

  def getNewsletterPosts(id: Option[Long]) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    val userId = request.identity.id.get
    def getExisting(id: Long): Future[NewsletterAndPosts] = {
      newsletters.getWithPostsById(Some(userId), id)
    }

    def create(): Future[NewsletterAndPosts] = {
      posts.getUnpublished(userId)
        .flatMap { posts =>
          val newsletter = NewsletterAndPosts.draft(userId, posts)
          newsletters.create(newsletter).map(nl => newsletter.copy(id = nl.id))
        }
    }

    id.fold(create())(getExisting)
      .flatMap(newsletter => ph.doNewsletter(newsletter, Target.SITE))
      .map { newsletter =>
        Ok(views.html.office.newsletterPosts(newsletter))
      }
  }

  def removePost(id: Long, postId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    val userId = request.identity.id.get
    newsletters.removePost(userId, id, postId)
      .map(_ => Redirect(controllers.office.routes.NewsletterController.getNewsletterPosts(Some(id))))
  }

  def addPost(id: Long, postId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    val userId = request.identity.id.get
    newsletters.addPost(userId, id, List(postId))
      .map(_ => Redirect(controllers.office.routes.NewsletterController.getNewsletterPosts(Some(id))))
  }

  def moveUpPost(id: Long, postId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    val userId = request.identity.id.get
    newsletters.moveUpPost(userId, id, postId)
      .map(_ => Redirect(controllers.office.routes.NewsletterController.getNewsletterPosts(Some(id))))
  }

  def moveDownPost(id: Long, postId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    val userId = request.identity.id.get
    newsletters.moveDownPost(userId, id, postId)
      .map(_ => Redirect(controllers.office.routes.NewsletterController.getNewsletterPosts(Some(id))))
  }

  //TODO: replace by ususal get newsletter
  def getNewsletterFinal(campaignId: Long, newsletterId: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    val userId = request.identity.id.get
    newsletters.getWithPostsById(Some(userId), newsletterId)
      .flatMap { nl =>
        ph.doNewsletter(nl, PublishingHouse.Target.SITE)
      }
      .map { newsletter =>
        Ok(views.html.office.newsletterFinal(newsletter, campaignId))
      }
  }

  def getHeaderForm(id: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    val userId = request.identity.id.get
    newsletters.getById(Some(userId), id)
      .map(nl => Ok(views.html.office.header(headerForm.fill(HeaderData(nl.id.get, nl.header)))))
  }

  def submitHeaderForm() = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    val userId = request.identity.id.get
    headerForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.info(s"bad newsletter header, form=$formWithErrors")
        Future(BadRequest(views.html.office.header(formWithErrors)))
      },
      form => {
        newsletters.getById(Some(userId), form.id)
          .flatMap { nl =>
            newsletters.update(nl.copy(header = Some(form.text)))
          }
          .map(_ => Redirect(routes.NewsletterController.getNewsletterPosts(Some(form.id))))
      }
    )
  }

  def getFooterForm(id: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    val userId = request.identity.id.get
    newsletters.getById(Some(userId), id)
      .map(nl => Ok(views.html.office.footer(footerForm.fill(FooterData(nl.id.get, nl.footer)))))
  }

  def submitFooterForm() = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    val userId = request.identity.id.get
    footerForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.info(s"bad newsletter footer, form=$formWithErrors")
        Future(BadRequest(views.html.office.footer(formWithErrors)))
      },
      form => {
        newsletters.getById(Some(userId), form.id)
          .flatMap { nl =>
            newsletters.update(nl.copy(footer = Some(form.text)))
          }
          .map(_ => Redirect(routes.NewsletterController.getNewsletterPosts(Some(form.id))))
      }
    )
  }
}
