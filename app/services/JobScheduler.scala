package services

import java.lang.invoke.MethodHandles
import javax.inject.{Inject, Singleton}

import jobs.CampaignJob
import models.Campaign
import org.slf4j.LoggerFactory
import repositories.{CampaignRepository, RecipientRepository}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
object JobScheduler {
}

@Singleton
class JobScheduler @Inject()(quartz: Quartz, campaigns: CampaignRepository, recipients: RecipientRepository)(implicit ec: ExecutionContext) {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  //TODO: personalized newsletter time
  //TODO: check if any campaigns already scheduled
  def schedule(campaign: Campaign) = {
    recipients.getById(campaign.recipientId)
      .flatMap { recipient =>
        Future.sequence(recipient.userIds
          .map { userId =>
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
