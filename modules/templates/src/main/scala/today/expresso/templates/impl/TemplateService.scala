package today.expresso.templates.impl

import javax.inject.{Inject, Singleton}

import play.api.i18n.{Langs, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import today.expresso.common.utils.UrlUtils
import today.expresso.templates.api.domain.{Edition, Target}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class TemplateService @Inject()(compiler: CompilerService,
                                urlUtils: UrlUtils,
                                langs: Langs,
                                messagesApi: MessagesApi)
                               (implicit ec: ExecutionContext) {

  val DEFAULT_LOCALE = "ru"

  def getNewsletterTemplate(e: Edition, t: Target.Value): Future[Html] = {
    compiler.doEdition(e, t)
      .map { template =>
        implicit val requestHeader: RequestHeader = urlUtils.mockRequestHeader
        implicit val messages: Messages = MessagesImpl(e.newsletter.locale, messagesApi)
        templates.email.html.newsletter(template)
      }
  }
}
