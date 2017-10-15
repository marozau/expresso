package controllers

import javax.inject.{Inject, Singleton}

import play.api.cache.Cached
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.{CampaignRepository, NewsletterRepository, PostRepository}
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
                                   cached: Cached,
                                 )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  // TODO: clear cache if needed and store current for the long time
  // TOTOD: clear cache when new campaign started
  def current() = cached("current").includeStatus(OK, 2.days) {
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
}
