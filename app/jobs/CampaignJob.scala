package jobs

import java.time.temporal.ChronoUnit
import java.util.Date
import javax.inject.Inject

import clients.Quartz
import models.Campaign
import org.quartz.core.jmx.JobDataMapSupport
import org.quartz.{JobBuilder, JobExecutionContext, Trigger, TriggerBuilder}
import services.{CampaignService, JobSchedulerService}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration

/**
  * @author im.
  */
object CampaignJob {

  def group = "campaign"

  def identity(campaign: Campaign) = s"$group-${campaign.id.get}"

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
      .startAt(Date.from(campaign.sendTime.toInstant.minus(15, ChronoUnit.MINUTES)))
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

  override protected def execute(context: JobExecutionContext, retry: Int): Unit = {
    val data = context.getMergedJobDataMap
    val campaignId = data.get("campaignId").asInstanceOf[Long]
    val schedule = campaignService.getById(campaignId).flatMap(jobSchedulerService.scheduleEdition)
    Await.result(schedule, Duration.Inf)

    //    quartz.pauseTriggers()
    //    quartz.scheduler.pauseTriggers(new GroupMatcher[TriggerKey]("asdf", StringMatcher.StringOperatorName.STARTS_WITH))
  }
}
