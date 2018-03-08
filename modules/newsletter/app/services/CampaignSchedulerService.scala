package services

import java.lang.invoke.MethodHandles
import java.util.Date
import javax.inject.{Inject, Singleton}

import clients.Quartz
import jobs.{CampaignCompleteJob, CampaignJob, EditionSendJob}
import models.{Campaign, Recipient}
import org.quartz.impl.matchers.GroupMatcher
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class CampaignSchedulerService @Inject()(quartz: Quartz, recipientService: RecipientService)(implicit ec: ExecutionContext) {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  //TODO: personalized newsletter time
  def scheduleCampaign(userId: Long, campaign: Campaign): Future[Date] = {
    val job = CampaignJob.buildJob(campaign)
    val trigger = CampaignJob.buildTrigger(userId, campaign)
    quartz.scheduleJob(job, trigger)
      .flatMap { _ =>
        val job = CampaignCompleteJob.buildJob(campaign)
        val trigger = CampaignCompleteJob.buildTrigger(campaign)
        quartz.scheduleJob(job, trigger)
      }
      .map { date =>
        logger.info(s"campaign scheduled, newsletterId=${campaign.newsletterId}, editionId=${campaign.editionId}, date=$date")
        date
      }
  }

  def scheduleEdition(userId: Long, campaign: Campaign) = {
    recipientService.getByNewsletterId(userId, campaign.newsletterId, Some(Recipient.Status.SUBSCRIBED))
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
            }.recover {
              case t: Throwable =>
                logger.error(s"failed to schedule job for $userId", t)
                Right(userId)
            }
          }
        )
      }
  }

  def stopCampaign() = {

  }

  def suspendCampaign(userId: Long, campaign: Campaign) = {
    for {
      _ <- quartz.pauseTriggers(GroupMatcher.groupEquals(CampaignJob.identity(campaign)))
      _ <- quartz.pauseTriggers(GroupMatcher.groupStartsWith(EditionSendJob.edition(campaign.editionId)))
    } yield Unit
  }

  def resumeCampaign(userId: Long, campaign: Campaign) = {
    for {
      _ <- quartz.resumeTriggers(GroupMatcher.groupEquals(CampaignJob.identity(campaign)))
      _ <- quartz.resumeTriggers(GroupMatcher.groupStartsWith(EditionSendJob.edition(campaign.editionId)))
    } yield Unit
  }
}
