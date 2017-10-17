package controllers

import javax.inject.{Inject, Singleton}

import models.Post
import play.api.cache.Cached
import play.api.mvc.{AbstractController, ControllerComponents}
import models.repositories.{CampaignRepository, NewsletterRepository, PostRepository}
import services.PublishingHouse

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * @author im.
  */
@Singleton
class ArchiveController @Inject()(
                                   cc: ControllerComponents,
                                   newsletters: NewsletterRepository,
                                   posts: PostRepository,
                                   campaigns: CampaignRepository,
                                   ph: PublishingHouse,
                                   cached: Cached
                                 )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  // TODO: clear cache if needed and store current for the long time
  // TOTO: clear cache when new campaign started
  // TODO: "current" key does not work, find key
  def current() = cached("current").includeStatus(OK, 10.seconds) {
    Action.async { implicit request =>
      campaigns.getLastSent()
        .flatMap { campaign =>
          newsletters.getWithPostsById(None, campaign.newsletterId)
            .flatMap { nl =>
              ph.doNewsletter(nl, PublishingHouse.Target.SITE)
            }
        }
        .map { newsletter =>
          Ok(views.html.site.newsletter(newsletter))
        }
    }
  }

  def post(newsletterId: Long, postId: Long) = cached(s"/post/$newsletterId/$postId").includeStatus(OK, 10.seconds) {
    Action.async { implicit request =>
      newsletters.getWithPostsById(None, newsletterId)
        .map { newsletter =>
          val archivePosts = {
            // get target post with id==postId with previous and next posts ordered by newsletter
            val ps = newsletter.posts.foldLeft(Seq.empty[Option[Post]]) { case (seq, post) =>
              if (post.id.contains(postId) || seq.size == 2) {
                if (seq.isEmpty) {
                  seq ++ Seq(None, Some(post))
                } else {
                  seq :+ Some(post)
                }
              } else {
                if (seq.size > 1) {
                  seq
                } else {
                  Seq(Some(post))
                }
              }
            }
            // reshape sequence to get nothing or exactly 3 elements.
            if (ps.size == 1) {
              Seq.empty[Option[Post]]
            } else if (ps.size == 2) {
              ps :+ None
            } else {
              ps
            }
          }
          (newsletter, archivePosts)
        }
        .flatMap { case (newsletter, archivePosts) =>
          ph.doOptionPosts(archivePosts.toList, PublishingHouse.Target.SITE)
            .map((newsletter, _))
        }
        .map { case (newsletter, archivePosts) =>
          if (archivePosts.isEmpty) {
            NotFound("error")
          } else {
            import implicits.NewsletterImplicits._
            val post = archivePosts(1)
            Ok(views.html.site.post(post.get, Some(newsletter), archivePosts.head, archivePosts.last))
          }
        }
    }
  }

  def newsletter(id: Long) = cached("/newsletter/" + id).includeStatus(OK, 10.seconds) {
    Action.async { implicit request =>
      newsletters.getWithPostsById(None, id)
        .flatMap(newsletter => ph.doNewsletter(newsletter, PublishingHouse.Target.SITE))
        .map(newsletter => Ok(views.html.site.newsletter(newsletter)))
    }
  }
}
