package controllers.newslet

import java.time.ZoneOffset
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import controllers.AssetsFinder
import forms.newslet.CampaignForm
import models.{Campaign, UserRole}
import modules.DefaultEnv
import org.webjars.play.WebJarsUtil
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{CampaignService, EditionService, JobSchedulerService}
import utils.WithRole

import scala.concurrent.{ExecutionContext, Future}


/**
  * @author im.
  */
@Singleton
class CampaignController @Inject()(
                                    silhouette: Silhouette[DefaultEnv],
                                    cc: ControllerComponents,
                                    campaigns: CampaignService,
                                    editions: EditionService,
                                    jobScheduler: JobSchedulerService)
                                  (implicit
                                   webJarsUtil: WebJarsUtil,
                                   assets: AssetsFinder,
                                   ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  import forms.newslet.CampaignForm._
  import implicits.CampaignImplicits._

  def getCampaignForm(id: Option[Long], editionId: Option[Long]) = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>

    val zoneOffset = ZoneOffset.ofHours(request.identity.timezone.getOrElse(0))

    def existing(id: Long) = {
      campaigns.getById(id)
        .map { campaign =>
          val zoneOffset = ZoneOffset.ofHours(request.identity.timezone.getOrElse(0))
          form.fill(campaign.copy(sendTime = campaign.sendTime.withZoneSameInstant(zoneOffset)))
        }
    }

    def empty() = {
      campaigns.getByEditionId(editionId.get)
        .flatMap { campaign =>
          if (campaign.isDefined) {
            Future.successful(form.fill(campaign.get.copy(sendTime = campaign.get.sendTime.withZoneSameInstant(zoneOffset))))
          } else {
            editions.getById(editionId.get).map { edition =>
              form.fill(CampaignForm.campaignDraft(request.identity, edition))
            }
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
        val campaign = Campaign(form.id, form.newsletterId, form.editionId, form.preview, form.sendTime.toDateTime)
        Logger.info(s"submit campaign, campaign=$campaign")
        campaigns.save(campaign)
          .map(c => Redirect(routes.EditionController.getNewsletterFinal(c.id.get, c.editionId)))
      }
    )
  }

  /**
    * NOTE: only EDITOR can approve campaign scheduling
    *
    * @param id campaign id you want to schedule
    * @return redirects to the newsletter list view
    */
  def scheduleCampaign(id: Long) = silhouette.SecuredAction(WithRole(UserRole.EDITOR)).async { implicit request =>
    campaigns.getById(id)
      .flatMap { campaign =>
        jobScheduler.scheduleCampaign(campaign)
      }
      .map { date =>
        Redirect(controllers.newslet.routes.NewsletterController.getList())
          .flashing("info" -> Messages("campaign was scheduled"))
      }
  }

}
