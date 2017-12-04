package controllers.site

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

import clients.PublishingHouse
import controllers.AssetsFinder
import models.PostView
import play.api.cache.Cached
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{EditionService, NewsletterService}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * @author im.
  */
@Singleton
class ArchiveController @Inject()(
                                   cc: ControllerComponents,
                                   newsletterService: NewsletterService,
                                   editionService: EditionService,
                                   ph: PublishingHouse,
                                   cached: Cached
                                 )(implicit
                                   ec: ExecutionContext,
                                   assets: AssetsFinder)
  extends AbstractController(cc) with I18nSupport {

  def list() = {
  }

  // TODO: clear cache if needed and store current for the long time
  // TOTO: clear cache when new campaign starts, "current" key does not work, find key
//  cached(s"current/$name").includeStatus(OK) {
  def current(name: String) =
    Action.async { implicit request =>
      newsletterService
        .getByNameUrl(name)
        .flatMap(newsletter => editionService.getCurrent(newsletter.id.get))
        .flatMap { edition =>
          ph.doEdition(edition, PublishingHouse.Target.SITE)
        }
        .map { edition =>
          Ok(views.html.site.newsletter(edition))
        }
    }
//  }
//  cached(s"/archive/$name/$date/$title").includeStatus(OK) {
  def post(name: String, date: String, title: String) =
    Action.async { implicit request =>
      newsletterService
        .getByNameUrl(name)
        .flatMap(newsletter =>
          editionService.getByDate(newsletter.id.get, LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE)))
        .flatMap { edition =>
          ph.doPostView(PostView(edition, title), PublishingHouse.Target.SITE)
        }
        .map { postView =>
          Ok(views.html.site.post(postView))
        }
    }
//  }

  def edition(name: String, date: String) = cached(s"/archive/$name/$date").includeStatus(OK) {
    Action.async {
      implicit request =>
        newsletterService
          .getByNameUrl(name)
          .flatMap(newsletter =>
            editionService.getByDate(newsletter.id.get, LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE)))
          .flatMap(edition => ph.doEdition(edition, PublishingHouse.Target.SITE))
          .map(edition => Ok(views.html.site.newsletter(edition)))
    }
  }
}
