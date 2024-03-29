package controllers.site

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

import controllers.AssetsFinder
import models.{PostView, Target}
import play.api.cache.Cached
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesImpl}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{CompilerService, EditionService, NewsletterService}

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
                                   ph: CompilerService,
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

  def post(name: String, date: String, title: String) = cached(s"/archive/$name/$date/$title").includeStatus(OK) {
    Action.async { implicit request =>
      newsletterService
        .getByNameUrl(name)
        .flatMap(newsletter =>
          editionService.getByDate(newsletter.id.get, LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE)))
        .flatMap { edition =>
          ph.doPostView(PostView(edition, title), Target.SITE)
        }
        .map { postView =>
          implicit val messages: Messages = {
            val lang = postView.edition.map(_.newsletter.lang).getOrElse(Lang.defaultLang)
            MessagesImpl(lang, messagesApi)
          }
          Ok(views.html.email.post(postView)(request, messages, assets))
        }
    }

  }

  def edition(name: String, date: String) = cached(s"/archive/$name/$date").includeStatus(OK) {
    Action.async {
      implicit request =>
        newsletterService
          .getByNameUrl(name)
          .flatMap(newsletter =>
            editionService.getByDate(newsletter.id.get, LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE)))
          .flatMap(edition => ph.doEdition(edition, Target.SITE))
          .map { edition =>
            implicit val messages: Messages = MessagesImpl(edition.newsletter.lang, messagesApi)
            Ok(views.html.email.newsletter(edition)(request, messages, assets))
          }
    }
  }
}
