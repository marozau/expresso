package controllers.site

import javax.inject.{Inject, Singleton}

import clients.MailChimp
import clients.MailChimp.MailChimpException
import controllers.AssetsFinder
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class MailChimpController @Inject()(cc: ControllerComponents,
                                    mailChimp: MailChimp)
                                   (implicit
                                    ec: ExecutionContext,
                                    assets: AssetsFinder)
  extends AbstractController(cc) with I18nSupport {

  import forms.site.Subscribe._


  def subscribe = Action.async { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => {
        Logger.error(formWithErrors.toString)
        Future.successful(
          Redirect(controllers.site.routes.Subscription.error(INTERNAL_SERVER_ERROR)).flashing("error" -> "Где-то закралась ошибка."))
      },
      form => {
        mailChimp.listMemberAdd(form.listId, form.email.get)
          .map { member =>
            Logger.info(s"new mailchimp subscription, listId=${form.listId}, id=${member.id}, email=${member.emailAddress}")
            Redirect(controllers.site.routes.Subscription.thanks())
          }
          .recover {
            case t: MailChimpException =>
              Logger.error(s"mailchimp subscription failed, error=${t.error}", t)
              Redirect(controllers.site.routes.Subscription.error(INTERNAL_SERVER_ERROR)).flashing("error" -> "Где-то закралась ошибка.")
            case t: Throwable =>
              Logger.error(s"mailchimp subscription failed", t)
              Redirect(controllers.site.routes.Subscription.error(INTERNAL_SERVER_ERROR)).flashing("error" -> "Где-то закралась ошибка.")
          }
      }
    )
  }
}
