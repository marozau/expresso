package services

import java.lang.invoke.MethodHandles
import java.util.Date
import javax.inject.{Inject, Singleton}

import clients.Quartz
import jobs.{CampaignCompleteJob, CampaignJob, EditionSendJob}
import models.{Campaign, Recipient}
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
      .setPendingStatus(campaign.editionId) //TODO: move next code to to transaction
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
      .map { date =>
        logger.info(s"campaign scheduled, newsletterId=${campaign.newsletterId}, editionId=${campaign.editionId}, date=$date")
        date
      }
//      .recover {
//        case t: Throwable =>
//          logger.error(s"failed to schedule campaign, campaign=$campaign", t)
//          campaigns.updateStatus(campaign.id.get, Campaign.Status.NEW)
//          throw t
//      }
  }

  def stopCampaign() = ???

  def pauseCampaign() = ???

  def scheduleEdition(campaign: Campaign) = {
    campaigns
      .setSendingStatus(campaign.editionId) //TODO: move next code to to transaction
      .flatMap { _ =>
        recipientService.getByNewsletterId(campaign.newsletterId, Recipient.Status.SUBSCRIBED)
          .flatMap { recipients =>
            Future.sequence(recipients
              .map { recipient =>
                val userId = recipient.userId
                val trigger = EditionSendJob.buildTrigger(userId, campaign)
                val job = EditionSendJob.buildJob(userId, campaign)
                quartz.scheduleJob(job, trigger) //TODO: remove old job if exists and create new one
                  .map { _ =>
                    logger.info(s"edition sending scheduled, newsletterId=${campaign.newsletterId}, editionId=${campaign.editionId}, userId=$userId")
                    Left(userId)
                  }
                  .recover {
                    case t: Throwable =>
                      logger.error(s"failed to schedule job for $userId", t)
                      Right(userId)
                  }
              }
            )
          }
      }
//      .recover {
//        case t: Throwable =>
//          logger.error(s"failed to schedule edition, campaign=$campaign", t)
//          campaigns.updateStatus(campaign.id.get, Campaign.Status.PENDING)
//          throw t
//      }
  }
}
