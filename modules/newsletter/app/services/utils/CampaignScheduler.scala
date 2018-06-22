package services.utils

import java.lang.invoke.MethodHandles
import java.util.Date

import javax.inject.{Inject, Singleton}
import clients.Quartz
import jobs.campaign.{CampaignJob, PendingJob, SendingJob, SentJob}
import org.quartz.impl.matchers.GroupMatcher
import org.slf4j.LoggerFactory
import services.RecipientService
import today.expresso.stream.domain.model.newsletter.{Campaign, Recipient}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class CampaignScheduler @Inject()(quartz: Quartz, recipientService: RecipientService)(implicit ec: ExecutionContext) {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  //TODO: personalized newsletter time
  def schedule(userId: Long, c: Campaign): Future[Date] = {
    val job = PendingJob.buildJob(userId, c)
    val trigger = PendingJob.buildTrigger(userId, c)
    quartz.scheduleJob(job, trigger)
      .flatMap { _ =>
        val job = SentJob.buildJob(userId, c)
        val trigger = SentJob.buildTrigger(userId, c)
        logger.info(s"schedule campaign, newsletterId=${c.newsletterId}, editionId=${c.editionId}, sendTime=${c.sendTime}")
        quartz.scheduleJob(job, trigger)
      }
  }

  def startSending(userId: Long, c: Campaign) = {
    recipientService.getByNewsletterId(userId, c.newsletterId, Some(Recipient.Status.SUBSCRIBED))
      .flatMap { recipients =>
        Future.sequence(recipients
          .map { recipient =>
            val userId = recipient.userId
            val trigger = SendingJob.buildTrigger(userId, c)
            val job = SendingJob.buildJob(userId, c)
            logger.info(s"schedule edition, newsletterId=${c.newsletterId}, editionId=${c.editionId}, sendTime=${c.sendTime}, userId=$userId")
            quartz.scheduleJob(job, trigger)
              .map { _ => Left(userId) }
              .recover {
                case t: Throwable =>
                  logger.error(s"failed to schedule edition, newsletterId=${c.newsletterId}, editionId=${c.editionId}, sendTime=${c.sendTime}, userId=$userId", t)
                  Right(userId)
              }
          }
        )
      }
      .map { status =>
        val failedUsers = status.filter(_.isRight).map(_.right)
        if (failedUsers.nonEmpty) {
          logger.warn(s"failed to schedule users size = ${failedUsers.size}")
          //TODO: reschedule for failed users
          //TODO: don't forget about campaign status
        }
        status
      }
  }

  def stop(c: Campaign) = {

  }

  def suspend(campaign: Campaign) = {
    quartz.pauseTriggers(GroupMatcher.groupEquals(CampaignJob.identity(campaign)))
  }

  def resume(campaign: Campaign) = {
    quartz.resumeTriggers(GroupMatcher.groupEquals(CampaignJob.identity(campaign)))
  }

  def suspendByUser(userId: Long) = {
    quartz.pauseJobs(GroupMatcher.jobGroupEquals(CampaignJob.userGroup(userId)))
  }

  def resumeByUser(userId: Long) = {
    quartz.resumeJobs(GroupMatcher.jobGroupEquals(CampaignJob.userGroup(userId)))
  }
}
