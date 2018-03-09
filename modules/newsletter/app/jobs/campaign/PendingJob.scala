package jobs.campaign

import java.lang.invoke.MethodHandles
import java.time.Instant
import java.util.Date
import javax.inject.Inject

import clients.Quartz
import exceptions.InvalidCampaignStatusException
import jobs.api.RecoveringJob
import models.Campaign
import org.quartz.{JobBuilder, JobExecutionContext, Trigger, TriggerBuilder}
import org.slf4j.LoggerFactory
import services.CampaignService

import scala.concurrent.{Await, ExecutionContext}

/**
  * @author im.
  */
object PendingJob {

  def identity(campaign: Campaign) = s"${CampaignJob.group}-${campaign.editionId}"

  def buildJobData(userId: Long, c: Campaign) = {
    // Prefef.long2Long is nedded to avoid error: the result type of an implicit conversion must be more specific than AnyRef
    val jobDataMap = Map[String, AnyRef](
      "userId" -> Predef.long2Long(userId),
      "editionId" -> Predef.long2Long(c.editionId),
      "newsletterId" -> Predef.long2Long(c.newsletterId),
      "sendTime" -> Predef.long2Long(c.sendTime.toEpochMilli),
      "status" -> c.status.toString
    )
    Quartz.newJobDataMap(jobDataMap)
  }

  def buildTrigger(userId: Long, c: Campaign): Trigger = {
    val jobData = buildJobData(userId, c)

    TriggerBuilder.newTrigger()
      .withIdentity(identity(c), CampaignJob.identity(c))
      .usingJobData(jobData)
      .startAt(Date.from(c.sendTime))
      .build()
  }

  def buildJob(campaign: Campaign) = {
    JobBuilder.newJob(classOf[PendingJob])
      .withIdentity(identity(campaign), CampaignJob.group)
      .requestRecovery
      .build
  }
}

class PendingJob @Inject()(quartz: Quartz, campaignService: CampaignService)(implicit ec: ExecutionContext)
  extends RecoveringJob(quartz) {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  override protected def execute(context: JobExecutionContext, retry: Int): Unit = {
    val data = context.getMergedJobDataMap
    val userId = data.get("userId").asInstanceOf[Long]
    val editionId = data.get("editionId").asInstanceOf[Long]
    val newsletterId = data.get("newsletterId").asInstanceOf[Long]
    val status = Campaign.Status.withName(data.get("status").asInstanceOf[String])
    val sendTime = Instant.ofEpochMilli(data.get("sendTime").asInstanceOf[Long])
    val campaign = Campaign(editionId, newsletterId, sendTime, status, None, None)
    logger.info(s"execute, userId=$userId, editionId=$editionId")
    try {
      import scala.concurrent.duration._
      Await.result(campaignService.startSending(userId, editionId), 1.minutes)
      //TODO: retry schedule for failed user ids
      logger.info("complete")
    } catch {
      case e: InvalidCampaignStatusException =>
        logger.error("campaign status error", e)
        logger.info("complete")
    }

    //    quartz.pauseTriggers()
    //    quartz.scheduler.pauseTriggers(new GroupMatcher[TriggerKey]("asdf", StringMatcher.StringOperatorName.STARTS_WITH))
  }
}
