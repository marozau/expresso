package controllers

import javax.inject.{Inject, Singleton}

import models.NewsletterAndPosts
import play.api.Logger
import play.api.cache.AsyncCacheApi
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}
import models.repositories.{NewsletterRepository, PostRepository}
import services.PublishingHouse
import services.PublishingHouse.Target
import utils.HtmlUtils

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
object NewsletterController {

  case class HeaderForm(id: Long, text: String)

  case class FooterForm(id: Long, text: String)

  val headerForm = Form(
    mapping(
      "id" -> longNumber,
      "text" -> nonEmptyText
    )(HeaderForm.apply)(HeaderForm.unapply)
  )

  val footerForm = Form(
    mapping(
      "id" -> longNumber,
      "text" -> nonEmptyText
    )(FooterForm.apply)(FooterForm.unapply)
  )
}

@Singleton
class NewsletterController @Inject()(
                                      cache: AsyncCacheApi,
                                      cc: ControllerComponents,
                                      newsletters: NewsletterRepository,
                                      posts: PostRepository,
                                      htmlUtils: HtmlUtils,
                                      ph: PublishingHouse
                                    )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  import NewsletterController._
  import implicits.NewsletterImplicits._

  val USER_ID = 10000000L

  def getNewsletterList() = Action.async { implicit request =>
    newsletters.getByUserId(USER_ID)
      .map { newsletters =>
        Ok(views.html.admin.newsletterList(newsletters))
      }
  }

  def getNewsletterPosts(id: Option[Long]) = Action.async { implicit request =>
    def getExisting(id: Long): Future[NewsletterAndPosts] = {
      newsletters.getWithPostsById(Some(USER_ID), id)
    }

    def create(): Future[NewsletterAndPosts] = {
      posts.getUnpublished(USER_ID)
        .flatMap { posts =>
          val newsletter = NewsletterAndPosts.draft(USER_ID, posts)
          newsletters.create(newsletter).map(nl => newsletter.copy(id = nl.id))
        }
    }

    id.fold(create())(getExisting)
      .flatMap(newsletter => ph.doNewsletter(newsletter, Target.SITE))
      .map { newsletter =>
        Ok(views.html.admin.newsletterPosts(newsletter))
      }
  }

  def removePost(id: Long, postId: Long) = Action.async { implicit request =>
    newsletters.removePost(USER_ID, id, postId)
      .map(_ => Redirect(routes.NewsletterController.getNewsletterPosts(Some(id))))
  }

  def addPost(id: Long, postId: Long) = Action.async { implicit request =>
    newsletters.addPost(USER_ID, id, List(postId))
      .map(_ => Redirect(routes.NewsletterController.getNewsletterPosts(Some(id))))
  }

  def moveUpPost(id: Long, postId: Long) = Action.async { implicit request =>
    newsletters.moveUpPost(USER_ID, id, postId)
      .map(_ => Redirect(routes.NewsletterController.getNewsletterPosts(Some(id))))
  }

  def moveDownPost(id: Long, postId: Long) = Action.async { implicit request =>
    newsletters.moveDownPost(USER_ID, id, postId)
      .map(_ => Redirect(routes.NewsletterController.getNewsletterPosts(Some(id))))
  }

  //TODO: replace by ususal get newsletter
  def getNewsletterFinal(campaignId: Long, newsletterId: Long) = Action.async { implicit request =>
    newsletters.getWithPostsById(Some(USER_ID), newsletterId)
      .flatMap { nl =>
        ph.doNewsletter(nl, PublishingHouse.Target.SITE)
      }
      .map { newsletter =>
        Ok(views.html.admin.newsletterFinal(newsletter, campaignId))
      }
  }

  def getHeaderForm(id: Long) = Action.async { implicit request =>
    newsletters.getById(Some(USER_ID), id)
      .map(nl => Ok(views.html.admin.header(headerForm.fill(HeaderForm(nl.id.get, nl.header)))))
  }

  def submitHeaderForm() = Action.async { implicit request =>
    headerForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.info(s"bad newsletter header, form=$formWithErrors")
        Future(BadRequest(views.html.admin.header(formWithErrors)))
      },
      form => {
        newsletters.getById(Some(USER_ID), form.id)
          .flatMap { nl =>
            newsletters.update(nl.copy(header = Some(form.text)))
          }
          .map(_ => Redirect(routes.NewsletterController.getNewsletterPosts(Some(form.id))))
      }
    )
  }

  def getFooterForm(id: Long) = Action.async { implicit request =>
    newsletters.getById(Some(USER_ID), id)
      .map(nl => Ok(views.html.admin.footer(footerForm.fill(FooterForm(nl.id.get, nl.footer)))))
  }

  def submitFooterForm() = Action.async { implicit request =>
    footerForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.info(s"bad newsletter footer, form=$formWithErrors")
        Future(BadRequest(views.html.admin.footer(formWithErrors)))
      },
      form => {
        newsletters.getById(Some(USER_ID), form.id)
          .flatMap { nl =>
            newsletters.update(nl.copy(footer = Some(form.text)))
          }
          .map(_ => Redirect(routes.NewsletterController.getNewsletterPosts(Some(form.id))))
      }
    )
  }
}
