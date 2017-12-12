package controllers.site

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

import clients.PublishingHouse
import controllers.AssetsFinder
import models.PostView
import play.api.cache.Cached
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesImpl}
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

  def current(name: String) =
    Action.async { implicit request =>
      newsletterService
        .getByNameUrl(name)
        .flatMap(newsletter => editionService.getCurrent(newsletter.id.get))
        .map { edition =>
          Redirect(
            controllers.site.routes.ArchiveController.edition(
              edition.newsletter.nameUrl, edition.date.format(DateTimeFormatter.BASIC_ISO_DATE))
          )
        }
    }

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
          implicit val messages: Messages = {
            val lang = postView.edition.map(_.newsletter.lang).getOrElse(Lang.defaultLang)
            MessagesImpl(lang, messagesApi)
          }
          Ok(views.html.site.post(postView)(request, messages, assets))
        }
    }

  //  }

  //  cached(s"/archive/$name/$date").includeStatus(OK) {
  def edition(name: String, date: String) =
    Action.async {
      implicit request =>
        newsletterService
          .getByNameUrl(name)
          .flatMap(newsletter =>
            editionService.getByDate(newsletter.id.get, LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE)))
          .flatMap(edition => ph.doEdition(edition, PublishingHouse.Target.SITE))
          .map{edition =>
            implicit val messages: Messages = MessagesImpl(edition.newsletter.lang, messagesApi)
            Ok(views.html.site.newsletter(edition)(request, messages, assets))
          }
    }

  //  }
}
