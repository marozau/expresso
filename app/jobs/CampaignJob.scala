package jobs

import java.lang.invoke.MethodHandles
import java.util.Date
import javax.inject.Inject

import models.Campaign
import org.quartz.core.jmx.JobDataMapSupport
import org.quartz._
import org.slf4j.LoggerFactory
import services.{Mailer, Quartz}

/**
  * @author im.
  */
object CampaignJob {

  def group = "campaign"
  def campaignIdentity(campaign: Campaign) = s"$group-${campaign.id.get}"
  def identity(userId: Long, campaign: Campaign) = s"${campaignIdentity(campaign)}-$userId"

  def buildTrigger(userId: Long, campaign: Campaign): Trigger = {
    // Prefef.long2Long is nedded to avoid error: the result type of an implicit conversion must be more specific than AnyRef
    val jobDataMap = Map[String, AnyRef](
      "userId" -> Predef.long2Long(userId),
      "campaignId" -> Predef.long2Long(campaign.id.get)
    )
    import scala.collection.JavaConverters._
    val jobData = JobDataMapSupport.newJobDataMap(jobDataMap.asJava)

    TriggerBuilder.newTrigger()
      .withIdentity(identity(userId, campaign), group)
      .usingJobData(jobData)
      .startAt(Date.from(campaign.sendTime.toInstant))
      .build()
  }

  def buildJob(userId: Long, campaign: Campaign): JobDetail = {
    JobBuilder.newJob(classOf[CampaignJob])
      .withIdentity(identity(userId, campaign), group)
      .requestRecovery
      .build
  }
}

class CampaignJob @Inject()(quartz: Quartz, emailService: Mailer) extends RecoveringJob(quartz) {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  override protected def execute(context: JobExecutionContext, retry: Int): Unit = {
    val data = context.getMergedJobDataMap
    val userId = data.get("userId").asInstanceOf[Long]
    val campaignId = data.get("campaignId").asInstanceOf[Long]
    logger.info(s"CampaignJob: userId=$userId, campaignId=$campaignId")
//    quartz.scheduler.pauseTriggers(new GroupMatcher[TriggerKey]("asdf", StringMatcher.StringOperatorName.STARTS_WITH))
  }

  //  // TODO: move to the service
  //  // TODO: use data converters and jobs from https://github.com/enragedginger/akka-quartz-scheduler/tree/master/src/main/scala
  //  private def scheduleNewsletter(nl: Newsletter) = {
  //    val timstamp = post.timestamp
  //    val job = JobBuilder.newJob(classOf[PostJob]).withIdentity("telegram-" + timstamp.toEpochMilli, "telegram").requestRecovery.build
  //
  //    val jobDataMap = Map[String, AnyRef](
  //      "picture" -> post.picture,
  //      "message" -> post.message
  //    )
  //    import scala.collection.JavaConverters._
  //    val jobData = JobDataMapSupport.newJobDataMap(jobDataMap.asJava)
  //
  //    val trigger = TriggerBuilder.newTrigger()
  //      .withIdentity("telegram-" + timstamp.toEpochMilli, "telegram")
  //      .usingJobData(jobData)
  //      .startAt(java.util.Date.from(timstamp))
  //      .build()
  //
  //    // Tell quartz to schedule the job using our trigger
  //    scheduler.scheduleJob(job, trigger)
  //  }
}
