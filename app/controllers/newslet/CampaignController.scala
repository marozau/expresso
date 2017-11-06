package controllers.newslet

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.AssetsFinder
import models.daos.CampaignDao
import models.{Campaign, UserRole}
import modules.DefaultEnv
import org.webjars.play.WebJarsUtil
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{JobSchedulerService, RecipientService}
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
                                    recipients: RecipientService,
                                    jobScheduler: JobSchedulerService)
                                  (implicit
                                   webJarsUtil: WebJarsUtil,
                                   assets: AssetsFinder,
                                   ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  import forms.newslet.CampaignForm._
  import implicits.CampaignImplicits._

  def getCampaignForm(id: Option[Long], editionId: Option[Long]) = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    def existing(id: Long) = {
      campaigns.getById(id)
        .map(campaign => form.fill(campaign))
    }

    def empty() = {
      campaigns.getByEditionId(editionId.get)
        .map { campaign =>
          if (campaign.isDefined) {
            form.fill(campaign.get)
          } else {
            form.fill(campaignDraft(editionId.get))
          }
        }
    }

    id.fold(empty())(existing)
      .map { f =>
        Ok(views.html.newslet.campaign(request.identity, f))
      }
  }

  def submitCampaignForm() = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => {
        Logger.info(s"bad campaign, form=$formWithErrors")
        Future.successful(BadRequest(views.html.newslet.campaign(request.identity, formWithErrors)))
      },
      form => {
        val campaign = Campaign(form.id, form.editionId, form.preview, form.sendTime.toDateTime)
        form.id.fold(campaigns.create(campaign))(_ => campaigns.update(campaign).map(_ => campaign))
          .map(c => Redirect(routes.EditionController.getNewsletterFinal(c.id.get, c.editionId)))
      }
    )
  }

  /**
    * only EDITOR can approve campaign scheduling
    *
    * @param id
    * @return
    */
  //TODO: add information about who schedule campaign
  def scheduleCampaign(id: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    campaigns.getById(id)
      .flatMap { campaign =>
        jobScheduler.schedule(campaign)
      }
      .flatMap(_ => campaigns.updateStatus(id, Campaign.Status.PENDING))
      .map(_ => Redirect(controllers.newslet.routes.NewsletterController.getList())
        .flashing("info" -> Messages("campaign was schedulled")))
  }

}
