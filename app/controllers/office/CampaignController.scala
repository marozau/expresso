package controllers.office

import javax.inject.{Inject, Singleton}

import clients.JobScheduler
import com.mohiva.play.silhouette.api.Silhouette
import models.{Campaign, UserRole}
import models.daos.{CampaignDao, RecipientDao}
import modules.DefaultEnv
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.WithRole

import scala.concurrent.{ExecutionContext, Future}


/**
  * @author im.
  */
@Singleton
class CampaignController @Inject()(
                                    silhouette: Silhouette[DefaultEnv],
                                    cc: ControllerComponents,
                                    campaigns: CampaignDao,
                                    recipients: RecipientDao,
                                    jobScheduler: JobScheduler)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  import forms.office.CampaignForm._
  import implicits.CampaignImplicits._

  def getCampaignForm(id: Option[Long], newsletterId: Option[Long]) = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    val userId = request.identity.id.get
    val recipient = recipients.getByUserId(userId)

    def existing(id: Long) = {
      campaigns.getById(id)
        .map(campaign => form.fill(campaign))
    }

    def empty() = {
      campaigns.getByNewsletterId(userId, newsletterId.get)
        .flatMap { campaign =>
          if (campaign.isDefined) {
            Future.successful(form.fill(campaign.get))
          } else {
            recipient.map { r =>
              val defaultList = r.filter(_.default.isDefined).filter(_.default.get)
              val default = if (defaultList.nonEmpty) defaultList.head else r.head
              form.fill(campaignDraft(newsletterId.get, default.id.get))
            }
          }
        }
    }

    id.fold(empty())(existing)
      .flatMap(f => recipient.map((f, _)))
      .map {
        case (form, rec) => Ok(views.html.office.campaign(form, rec))
      }
  }

  def submitCampaignForm() = silhouette.SecuredAction(WithRole(UserRole.EDITOR, UserRole.WRITER)).async { implicit request =>
    val userId = request.identity.id.get
    form.bindFromRequest.fold(
      formWithErrors => {
        Logger.info(s"bad campaign, form=$formWithErrors")
        Future.successful(BadRequest(formWithErrors.toString))
        //        Future(BadRequest(views.html.office.campaign(formWithErrors)))
      },
      form => {
        val campaign = Campaign(form.id, userId, form.newsletterId, form.name, form.subject, form.preview, form.fromName, form.fromEmail,
          form.sendTime.toDateTime, form.recipients)
        form.id.fold(campaigns.create(campaign))(_ => campaigns.update(campaign).map(_ => campaign))
          .map(c => Redirect(routes.NewsletterController.getNewsletterFinal(c.id.get, c.newsletterId)))
      }
    )
  }

  /**
    * only EDITOR can approve campaign scheduling
    * @param id
    * @return
    */
  //TODO: add information about who schedule campaign
  def scheduleCampaign(id: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    campaigns.getById(id)
      .flatMap { campaign =>
        jobScheduler.schedule(campaign)
          .map(_ => campaign)
      }
      .flatMap(campaign => campaigns.update(campaign.copy(status = Campaign.Status.PENDING)))
      .map(_ => Ok("Done"))
  }

}
