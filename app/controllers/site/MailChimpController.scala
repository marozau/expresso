package controllers.site

import javax.inject.{Inject, Singleton}

import clients.MailChimp
import clients.MailChimp.MailChimpException
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class MailChimpController @Inject()(cc: ControllerComponents,
                                    mailChimp: MailChimp)
                                   (implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {


  def subscribe(listId: String, email: String) = Action.async { implicit request =>
    mailChimp.listMemberAdd(listId, email)
      .map { member =>
        Logger.info(s"new mailchimp subscription, id=${member.id}, email=${member.emailAddress}")
        Ok("Почти готово! Теперь проверь свой почтовый ящик.")
      }
      .recover {
        case t: MailChimpException =>
          Logger.error(s"mailchimp subscription failed, error=${t.error}", t)
          InternalServerError("Где-то закралась ошибка. Попробуй еще раз.")
        case t: Throwable =>
          Logger.error(s"mailchimp subscription failed", t)
          InternalServerError("Где-то закралась ошибка. Попробуй еще раз.")
      }
  }
}
