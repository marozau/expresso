package controllers.office

import javax.inject.{Inject, Singleton}

import clients.PublishingHouse
import clients.PublishingHouse.Target
import com.mohiva.play.silhouette.api.Silhouette
import models.daos.PostDao
import models.{Post, UserRole}
import modules.DefaultEnv
import play.api.Logger
import play.api.cache._
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.{HtmlUtils, WithRole}

import scala.concurrent.{ExecutionContext, Future}


/**
  * @author im.
  */
@Singleton
class PostController @Inject()(
                                silhouette: Silhouette[DefaultEnv],
                                cache: AsyncCacheApi,
                                cc: ControllerComponents,
                                htmlUtils: HtmlUtils,
                                ph: PublishingHouse,
                                posts: PostDao)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  import forms.office.PostForm._

  def getPostForm(id: Option[Long], newsletterId: Option[Long]) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    def getExisting(id: Long) = {
      posts.getById(id).map(p => form.fill(p))
    }

    def create(newsletterId: Option[Long]) = {
      Future(form.fill(Data(None, newsletterId, "", "", "", List.empty)))
    }

    id.fold(create(newsletterId))(getExisting)
      .map(f => Ok(views.html.office.post(f)))
  }

  def submitPostForm() = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    val userId = request.identity.id.get
    form.bindFromRequest.fold(
      formWithErrors => {
        Logger.error(formWithErrors.toString)
        Future(BadRequest(views.html.office.post(formWithErrors)))
      },
      form => {
        val post = Post(form.id, userId, form.newsletterId, form.title, form.annotation, form.body, form.refs.map(_.toString))
        form.id.fold(posts.create(post).map(_.id))(id => posts.update(post).map(_ => Some(id)))
          .map { postId =>
            form.newsletterId
              .fold {
                Redirect(routes.PostController.showPost(postId.get))
              } { newsletterId =>
                Redirect(routes.NewsletterController.addPost(newsletterId, postId.get))
              }
          }
      }
    )
  }

  def showPost(id: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    posts.getById(id)
      .flatMap(post => ph.doPost(post, Target.SITE))
      .map(post => Ok(views.html.site.post(post)))
  }
}
