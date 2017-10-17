package controllers

import java.lang.ProcessBuilder.Redirect
import java.net.URL
import javax.inject.{Inject, Singleton}

import models.Post
import play.api.Logger
import play.api.cache._
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import repositories.PostRepository
import services.PublishingHouse
import services.PublishingHouse.Target
import utils.HtmlUtils

import scala.concurrent.{ExecutionContext, Future}


/**
  * @author im.
  */
object PostController {

  case class PostForm(id: Option[Long], newsletterId: Option[Long], title: String, annotation: String, body: String, refs: List[URL])

  case class SelectedPostForm(id: Long, selected: Boolean)

  case class PostSelectorForm(all: Boolean, elems: List[SelectedPostForm])

  import utils.HtmlUtils._
  val postForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "newsletterId" -> optional(longNumber),
      "title" -> nonEmptyText,
      "annotation" -> nonEmptyText,
      "body" -> nonEmptyText,
      "refs" -> list(of[URL])
    )(PostForm.apply)(PostForm.unapply)
  )

  val postSelectorForm = Form(
    mapping(
      "all" -> boolean,
      "posts" -> list(
        mapping(
          "id" -> longNumber,
          "selected" -> boolean
        )(SelectedPostForm.apply)(SelectedPostForm.unapply))
    )(PostSelectorForm.apply)(PostSelectorForm.unapply)
  )
}

@Singleton
class PostController @Inject()(
                                cache: AsyncCacheApi,
                                cc: ControllerComponents,
                                htmlUtils: HtmlUtils,
                                ph: PublishingHouse,
                                posts: PostRepository)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  import PostController._
  import implicits.PostImplicits._

  val USER_ID = 10000000L

  def getPostForm(id: Option[Long], newsletterId: Option[Long]) = Action.async { implicit request =>
    def getExisting(id: Long) = {
      posts.getById(id).map(p => postForm.fill(p))
    }

    def create(newsletterId: Option[Long]) = {
      Future(postForm.fill(PostForm(None, newsletterId, "", "", "", List.empty)))
    }

    val form = id.fold(create(newsletterId))(getExisting)
    form.map(f => Ok(views.html.admin.post(f)))
  }

  def submitPostForm() = Action.async { implicit request =>
    postForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.error(formWithErrors.toString)
        Future(BadRequest(views.html.admin.post(formWithErrors)))
      },
      form => {
        val post = Post(form.id, USER_ID, form.newsletterId, form.title, form.annotation, form.body, form.refs.map(_.toString))
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

  def showPost(id: Long) = Action.async { implicit request =>
    posts.getById(id)
      .flatMap(post => ph.doPost(post, Target.SITE))
      .map(post => Ok(views.html.site.post(post)))
  }

  def getPostList(drop: Int, take: Int) = Action.async { implicit request =>
    //TODO: get selected forms from cache
    posts.getList(USER_ID, drop, take)
      .map(ps => Ok(views.html.admin.postSelector(ps, postSelectorForm)))
  }

  private def validateAndCachePostListForm(redirect: Redirect)(implicit request: Request[AnyContent]) = {
    postSelectorForm.bindFromRequest.fold(
      formWithErrors => {
        Future(BadRequest(formWithErrors.toString))
        //        BadRequest(views.html.admin.postSelector(posts, formWithErrors))
      },
      form => {
        val key = s"$USER_ID.nl.post.list"
        cache.get[List[Long]](key)
          .map { postIds =>
            val selectedPosts = form.elems.filter(_.selected).map(_.id)
            postIds.fold(selectedPosts)(ids => (ids ++ selectedPosts).distinct)
          }.map(postIds => cache.set(key, postIds))
          .map(_ => redirect)
      }
    )
  }

  def submitPostList() = Action.async { implicit request =>
    Future(Ok("OK"))
    //    postSelectorForm.bindFromRequest.fold(
    //      formWithErrors => {
    //        Future(BadRequest(formWithErrors.toString))
    //        //        BadRequest(views.html.admin.postSelector(posts, formWithErrors))
    //      },
    //      form => {
    //        val key = s"$USER_ID.nl.post.list"
    //        cache.get[List[Long]](key)
    //          .map { postIds =>
    //            val selectedPosts = form.elems.filter(_.selected).map(_.id)
    //            postIds.fold(selectedPosts)(ids => (ids ++ selectedPosts).distinct)
    //          }.map(postIds => cache.set(key, postIds))
    //          .map(_ => Redirect(routes.NewsletterController.getNewsletterForm(None)))
    //      }
    //    )
  }

  //TODO: post list pagination
  //  def nextPostList() = Action.async { implicit request =>
  //    validateAndCachePostListForm(Redirect(routes.NewsletterController.getNewsletterForm(None)))
  //  }

  //  def compileTest() = Action.async {
  //    val test =
  //      """
  //                  @import _root_.services.Tracking
  //                  @this(implicit track: Tracking)
  //                  @()
  //                 <!DOCTYPE html>
  //                 <html>
  //                   <head>
  //                     <title>title</title>
  //                   </head>
  //                   <body>
  //                     <section class="content">@href("https://www.google.com", "google")</section>
  //                   </body>
  //                 </html>
  //               """
  //    compiler.compile(test, Some("views.html.email"))().map(r => Ok(r))
  //  }
}
