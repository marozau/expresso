package services

import java.util.{Date, Properties}
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import org.quartz.impl.StdSchedulerFactory
import org.quartz.spi.{JobFactory, TriggerFiredBundle}
import org.quartz._
import org.quartz.impl.matchers.GroupMatcher
import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Logger, Play}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class Quartz @Inject()(appLifecycle: ApplicationLifecycle, config: Configuration, actorSystem: ActorSystem)(implicit ec: ExecutionContext) {

  private val blockingExecutionContext = actorSystem.dispatchers.lookup("quartz.blocking-dispatcher")

  private val schedulerFactory = new StdSchedulerFactory()
  schedulerFactory.initialize {
    import scala.collection.JavaConverters._
    val props = new Properties()
    val map: Map[String, Object] = config.get[Configuration]("quartz").underlying.entrySet().asScala
      .map({ entry =>
        entry.getKey -> entry.getValue.unwrapped()
      })(collection.breakOut)
    props.putAll(map.asJava)
    props
  }

  private val scheduler: Scheduler = schedulerFactory.getScheduler
  scheduler.setJobFactory(new GuiceJobFactory())

  scheduler.start()
  Logger.info(s"$getClass: started")
  appLifecycle.addStopHook { () =>
    Future {
      Logger.info(s"$getClass: shutdown")
      scheduler.shutdown(true)
    }
  }

  def scheduleJobBlocking(trigger: Trigger): Date = {
    scheduler.scheduleJob(trigger)
  }

  def scheduleJob(jobDetail: JobDetail, trigger: Trigger): Future[Date] = {
    Future(scheduler.scheduleJob(jobDetail, trigger))(blockingExecutionContext)
  }

  def rescheduleJob(triggerKey: TriggerKey, newTrigger: Trigger): Future[Date] = {
    Future(scheduler.rescheduleJob(triggerKey, newTrigger))(blockingExecutionContext)
  }

  def pauseTriggers(matcher: GroupMatcher[TriggerKey]): Future[Unit] = {
    Future(scheduler.pauseTriggers(matcher))(blockingExecutionContext)
  }

  def deleteJob(jobKey: JobKey): Future[Boolean] = {
    Future(scheduler.deleteJob(jobKey))(blockingExecutionContext)
  }

  def checkExists(triggerKey: TriggerKey): Future[Boolean] = {
    Future(scheduler.checkExists(triggerKey))(blockingExecutionContext)
  }
}

class GuiceJobFactory extends JobFactory {
  final val log = Logger(getClass)

  @throws[SchedulerException]
  def newJob(bundle: TriggerFiredBundle, Scheduler: Scheduler): Job = {

    val jobDetail = bundle.getJobDetail
    val jobClass = jobDetail.getJobClass

    try {
      if (log.isDebugEnabled) log.debug("Producing instance of Job '" + jobDetail.getKey + "', class=" + jobClass.getName)
      Play.current.injector.instanceOf(jobClass)

    } catch {
      case e: Exception => {
        throw new SchedulerException("Problem instantiating class '" + jobDetail.getJobClass.getName + "'", e)
      }
    }
  }
}

