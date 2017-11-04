package services

import java.lang.invoke.MethodHandles
import javax.inject.{Inject, Singleton}

import clients.Quartz
import jobs.CampaignJob
import models.Campaign
import models.daos.CampaignDao
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
object JobSchedulerService {
}

@Singleton
class JobSchedulerService @Inject()(quartz: Quartz, campaigns: CampaignDao, recipientService: RecipientService)(implicit ec: ExecutionContext) {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  //TODO: personalized newsletter time
  //TODO: check if any campaigns already scheduled
  def schedule(campaign: Campaign) = {
    //    campaigns.update(campaign.copy(status = Campaign.Status.PENDING)

    recipientService.getEditionRecipients(campaign.editionId)
      .flatMap { recipients =>
        Future.sequence(recipients
          .map { recipient =>
            val userId = recipient.userId
            val job = CampaignJob.buildJob(userId, campaign)
            val trigger = CampaignJob.buildTrigger(userId, campaign)
            quartz.scheduleJob(job, trigger)
              .map(date => (userId, Some(date)))
              .recover {
                case t: Throwable =>
                  logger.error(s"failed to schedule job for $userId", t)
                  (userId, None)
              }
          })
      }
  }
}
