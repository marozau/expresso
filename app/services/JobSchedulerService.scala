package services

import java.lang.invoke.MethodHandles
import java.util.Date
import javax.inject.{Inject, Singleton}

import clients.Quartz
import jobs.{CampaignCompleteJob, CampaignJob, EditionSendJob}
import models.Campaign
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class JobSchedulerService @Inject()(quartz: Quartz, campaigns: CampaignService, recipientService: RecipientService)(implicit ec: ExecutionContext) {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  //TODO: personalized newsletter time
  def scheduleCampaign(campaign: Campaign): Future[Date] = {
    campaigns
      .setPendingStatus(campaign.id.get)
      .flatMap { _ =>
        val job = CampaignJob.buildJob(campaign)
        val trigger = CampaignJob.buildTrigger(campaign)
        quartz.scheduleJob(job, trigger)
      }
      .flatMap { _ =>
        val job = CampaignCompleteJob.buildJob(campaign)
        val trigger = CampaignCompleteJob.buildTrigger(campaign)
        quartz.scheduleJob(job, trigger)
      }
      .recover {
        case t: Throwable =>
          logger.error(s"failed to schedule campaign, campaign=$campaign", t)
          campaigns.updateStatus(campaign.id.get, Campaign.Status.NEW)
          throw t
      }
  }

  def stopCampaign() = ???

  def pauseCampaign() = ???

  def scheduleEdition(campaign: Campaign): Future[Seq[(Long, Option[Date])]] = {
    campaigns
      .setSendingStatus(campaign.id.get)
      .flatMap { _ =>
        recipientService.getNewsletterRecipients(campaign.newsletterId)
          .flatMap { recipients =>
            Future.sequence(recipients
              .map { recipient =>
                val userId = recipient.userId
                val trigger = EditionSendJob.buildTrigger(userId, campaign)
                val job = EditionSendJob.buildJob(userId, campaign)
                quartz.scheduleJob(job, trigger)
                  .map(date => (userId, Some(date)))
                  .recover {
                    case t: Throwable =>
                      logger.error(s"failed to schedule job for $userId", t)
                      (userId, None)
                  }
              }
            )
          }
      }
      .recover {
        case t: Throwable =>
          logger.error(s"failed to schedule edition, campaign=$campaign", t)
          campaigns.updateStatus(campaign.id.get, Campaign.Status.PENDING)
          throw t
      }
  }
}
