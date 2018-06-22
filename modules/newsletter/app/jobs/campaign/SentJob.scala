package jobs.campaign

import java.lang.invoke.MethodHandles
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

import javax.inject.Inject
import clients.Quartz
import jobs.api.RecoveringJob
import org.quartz.core.jmx.JobDataMapSupport
import org.quartz.impl.matchers.GroupMatcher
import org.quartz.{JobBuilder, JobExecutionContext, Trigger, TriggerBuilder}
import org.slf4j.LoggerFactory
import services.CampaignService
import today.expresso.stream.domain.model.newsletter.Campaign

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * @author im.
  */
object SentJob {

  def name(c: Campaign) = s"${CampaignJob.identity(c)}-complete"

  def buildJobData(userId: Long, c: Campaign) = {
    // Prefef.long2Long is nedded to avoid error: the result type of an implicit conversion must be more specific than AnyRef
    val jobDataMap = Map[String, AnyRef](
      "userId" -> Predef.long2Long(userId),
      "editionId" -> Predef.long2Long(c.editionId),
      "newsletterId" -> Predef.long2Long(c.newsletterId),
      "sendTime" -> Predef.long2Long(c.sendTime.toEpochMilli),
      "status" -> c.status.toString,
    )
    Quartz.newJobDataMap(jobDataMap)
  }

  def buildTrigger(userId: Long, c: Campaign): Trigger = {

    val jobData = buildJobData(userId, c)

    //TODO: should be the same group for all CampaignJob, EditionJob and CampaignCompleteJob
    TriggerBuilder.newTrigger()
      .withIdentity(name(c), CampaignJob.identity(c))
      .usingJobData(jobData)
      .startAt(Date.from(c.sendTime.plus(5, ChronoUnit.MINUTES)))
      .build()
  }

  def buildJob(userId: Long, c: Campaign) = {
    JobBuilder.newJob(classOf[SentJob])
      .withIdentity(name(c), CampaignJob.userGroup(userId))
      .requestRecovery
      .build
  }
}

class SentJob @Inject()(quartz: Quartz, campaignService: CampaignService) extends RecoveringJob(quartz) {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  override protected def execute(context: JobExecutionContext, retry: Int): Unit = {
    val data = context.getMergedJobDataMap
    val userId = data.get("userId").asInstanceOf[Long]
    val editionId = data.get("editionId").asInstanceOf[Long]
    val newsletterId = data.get("newsletterId").asInstanceOf[Long]
    val status = Campaign.Status.withName(data.get("status").asInstanceOf[String])
    val sendTime = Instant.ofEpochMilli(data.get("sendTime").asInstanceOf[Long])
    val campaign = Campaign(userId, editionId, newsletterId, sendTime, status, None, None)
    logger.info(s"execute CampaignCompleteJob, editionId=$editionId")
    //TODO: forced campaign completion after timeout or N attempts
    val result = Await.result(campaignService.completeCampaign(userId, editionId, forced = false), Duration.Inf)
    if (result.status != Campaign.Status.SENT) {
      logger.warn(s"reschedule CampaignCompleteJob, editionId=$editionId")
      val trigger = TriggerBuilder.newTrigger()
        .withIdentity(SentJob.name(campaign), CampaignJob.identity(campaign))
        .usingJobData(data)
        .startAt(Date.from(Instant.now().plus(1, ChronoUnit.MINUTES)))
        .build()
      quartz.rescheduleJobBlocking(trigger.getKey, trigger)
    }
    logger.info("complete")
  }
}
