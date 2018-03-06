package jobs

import java.lang.invoke.MethodHandles
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.inject.Inject

import clients.Quartz
import models.Campaign
import org.quartz.core.jmx.JobDataMapSupport
import org.quartz.impl.matchers.GroupMatcher
import org.quartz.{JobBuilder, JobExecutionContext, Trigger, TriggerBuilder}
import org.slf4j.LoggerFactory
import services.CampaignService

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * @author im.
  */
object CampaignCompleteJob {

  def group = "campaign"

  def identity(editionId: Long) = s"$group-complete-$editionId"

  def buildTrigger(campaign: Campaign): Trigger = {
    // Prefef.long2Long is nedded to avoid error: the result type of an implicit conversion must be more specific than AnyRef
    val jobDataMap = Map[String, AnyRef](
      "editionId" -> Predef.long2Long(campaign.editionId),
    )
    import scala.collection.JavaConverters._
    val jobData = JobDataMapSupport.newJobDataMap(jobDataMap.asJava)

    TriggerBuilder.newTrigger()
      .withIdentity(identity(campaign.editionId), group)
      .usingJobData(jobData)
      .startAt(Date.from(campaign.sendTime.plus(5, ChronoUnit.MINUTES)))
      .build()
  }

  def buildJob(campaign: Campaign) = {
    JobBuilder.newJob(classOf[CampaignCompleteJob])
      .withIdentity(identity(campaign.editionId), group)
      .requestRecovery
      .build
  }
}

class CampaignCompleteJob @Inject()(quartz: Quartz, campaignService: CampaignService) extends RecoveringJob(quartz) {

  import jobs.CampaignCompleteJob._

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  override protected def execute(context: JobExecutionContext, retry: Int): Unit = {
    val data = context.getMergedJobDataMap
    val editionId = data.get("editionId").asInstanceOf[Long]
    logger.info(s"execute CampaignCompleteJob, editionId=$editionId")
    val jobKeysFuture = quartz.getJobKeys(GroupMatcher.groupStartsWith(EditionSendJob.edition(editionId)))
    val jobKeys = Await.result(jobKeysFuture, Duration.Inf)
    if (jobKeys.isEmpty) {
      Await.result(campaignService.setSentStatus(editionId), Duration.Inf)
    } else {
      logger.warn(s"reschedule CampaignCompleteJob, editionId=$editionId, jobKeys.size=${jobKeys.size()}")
      val trigger = TriggerBuilder.newTrigger()
        .withIdentity(identity(editionId), group)
        .usingJobData(data)
        .startAt(Date.from(Instant.now().plus(1, ChronoUnit.MINUTES)))
        .build()
      quartz.rescheduleJobBlocking(trigger.getKey, trigger)
    }
    //    quartz.pauseTriggers()
    //    quartz.scheduler.pauseTriggers(new GroupMatcher[TriggerKey]("asdf", StringMatcher.StringOperatorName.STARTS_WITH))
  }
}
