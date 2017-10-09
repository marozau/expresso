package jobs

import java.time.Instant
import java.util.Date
import java.util.concurrent.ThreadLocalRandom

import com.google.common.base.Joiner
import org.quartz._
import play.api.Logger
import services.Quartz

/**
  * @author im.
  */
object RecoveringJob {
  val RECOVERY_KEY: String = "_recovery"
}

abstract class RecoveringJob(quartz: Quartz) extends Job {

  import RecoveringJob._

  @throws[JobExecutionException]
  protected def execute(context: JobExecutionContext, retry: Int): Unit

  override def execute(context: JobExecutionContext): Unit = {
    Logger.debug(s"executing job=${context.getJobDetail.getJobClass}, data=${Joiner.on(",").withKeyValueSeparator("=").join(context.getMergedJobDataMap)}")

    val recoveryData = context.getMergedJobDataMap.get(RECOVERY_KEY)
    var retry: Int = if (recoveryData == null) 0 else recoveryData.asInstanceOf[Int]
    try {
      execute(context, retry)
    } catch {
      case e: JobExecutionException => throw e;
      case t: Throwable =>
        // TODO There must be some upper threshold for the number of retries depending on the type of the job
        retry += 1
        Logger.error(s"failed to process job ${context.getJobDetail.getJobClass.getName} - retry $retry", t)


        val jobKey = context.getJobDetail.getKey
        val trigger = context.getTrigger
        val triggerKey = trigger.getKey

        val recoveryTrigger = TriggerBuilder
          .newTrigger.forJob(jobKey)
          .usingJobData(RECOVERY_KEY, new Integer(retry))
          .usingJobData(context.getMergedJobDataMap)
          .startAt(calculateNextFireTime(retry))
          .withIdentity(buildNewTriggerKey(triggerKey, jobKey, retry))
          .build()

        try {
          quartz.scheduleJobBlocking(recoveryTrigger)
        } catch {
          case e1: SchedulerException =>
            throw new RuntimeException(e1)
        }
    }
  }

  private def calculateNextFireTime(retry: Int) = { // exponential backoff with random jitter
    val backoffInterval = ThreadLocalRandom.current.nextInt(1 << (retry - 1))
    Date.from(Instant.now.plusSeconds(backoffInterval))
  }

  private def buildNewTriggerKey(triggerKey: TriggerKey, jobKey: JobKey, retry: Int) = {
    new TriggerKey(buildName(triggerKey, jobKey, retry),
      if (retry > 1) triggerKey.getGroup else triggerKey.getGroup + ".recovery")
  }

  private def buildName(triggerKey: TriggerKey, jobKey: JobKey, retry: Int) = {
    if ("RECOVERING_JOBS" == triggerKey.getGroup) {
      jobKey.getName + "#" + retry
    } else {
      val currentName = triggerKey.getName
      if (retry == 1) currentName + "#1"
      else currentName.substring(0, currentName.length - Integer.toString(retry - 1).length) + retry
    }
  }
}
