package jobs

import java.lang.invoke.MethodHandles
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.inject.Inject

import clients.Quartz
import today.expresso.common.exceptions.InvalidCampaignScheduleException
import models.Campaign
import org.quartz.core.jmx.JobDataMapSupport
import org.quartz.{JobBuilder, JobExecutionContext, Trigger, TriggerBuilder}
import org.slf4j.LoggerFactory
import services.{CampaignService, JobSchedulerService}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

/**
  * @author im.
  */
object CampaignJob {

  def group = "campaign"

  def identity(campaign: Campaign) = s"$group-${campaign.id.get}"

  def toInstant(campaign: Campaign) = {
    val campaignTimestamp = campaign.sendTime.toInstant
    val sendTimestamp = campaignTimestamp.minus(15, ChronoUnit.MINUTES)
    val now = Instant.now()
    if (campaignTimestamp.compareTo(now) < 0) throw InvalidCampaignScheduleException("campaign timestamp must be in the future")
    if (sendTimestamp.compareTo(now) < 0) now
    else sendTimestamp
  }

  def buildTrigger(campaign: Campaign): Trigger = {
    // Prefef.long2Long is nedded to avoid error: the result type of an implicit conversion must be more specific than AnyRef
    val jobDataMap = Map[String, AnyRef](
      "campaignId" -> Predef.long2Long(campaign.id.get),
    )
    import scala.collection.JavaConverters._
    val jobData = JobDataMapSupport.newJobDataMap(jobDataMap.asJava)

    TriggerBuilder.newTrigger()
      .withIdentity(identity(campaign), group)
      .usingJobData(jobData)
      .startAt(Date.from(toInstant(campaign)))
      .build()
  }

  def buildJob(campaign: Campaign) = {
    JobBuilder.newJob(classOf[CampaignJob])
      .withIdentity(identity(campaign), group)
      .requestRecovery
      .build
  }
}

class CampaignJob @Inject()(quartz: Quartz,
                            jobSchedulerService: JobSchedulerService,
                            campaignService: CampaignService)
                           (implicit ec: ExecutionContext)
  extends RecoveringJob(quartz) {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  override protected def execute(context: JobExecutionContext, retry: Int): Unit = {
    val data = context.getMergedJobDataMap
    val campaignId = data.get("campaignId").asInstanceOf[Long]
    logger.info(s"execute CampaignJob, campaignId=$campaignId")
    val schedule = campaignService.getById(campaignId).flatMap(jobSchedulerService.scheduleEdition)
    //TODO: retry schedule for failed user ids
    Await.result(schedule, Duration.Inf)

    //    quartz.pauseTriggers()
    //    quartz.scheduler.pauseTriggers(new GroupMatcher[TriggerKey]("asdf", StringMatcher.StringOperatorName.STARTS_WITH))
  }
}
