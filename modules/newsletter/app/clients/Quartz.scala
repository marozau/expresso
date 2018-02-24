package clients

import java.util.{Date, Properties}
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import org.quartz._
import org.quartz.impl.StdSchedulerFactory
import org.quartz.impl.matchers.GroupMatcher
import org.quartz.spi.{JobFactory, TriggerFiredBundle}
import play.api.inject.{ApplicationLifecycle, Injector}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class Quartz @Inject()(appLifecycle: ApplicationLifecycle,
                       config: Configuration,
                       actorSystem: ActorSystem,
                       jobFactory: JobFactory)(implicit ec: ExecutionContext) {

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
  scheduler.setJobFactory(jobFactory)

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

  def rescheduleJobBlocking(triggerKey: TriggerKey, newTrigger: Trigger): Date = {
    scheduler.rescheduleJob(triggerKey, newTrigger)
  }

  def scheduleUniqueJob(jobDetail: JobDetail, trigger: Trigger): Future[Date] = {
    Future(scheduler.scheduleJob(jobDetail, trigger))(blockingExecutionContext)
  }

  def scheduleJob(jobDetail: JobDetail, trigger: Trigger): Future[Date] = {
    checkExists(trigger.getKey)
      .flatMap { exists =>
        if (exists) rescheduleJob(trigger.getKey, trigger)
        else scheduleUniqueJob(jobDetail, trigger)
      }
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

  def getJobKeys(matcher: GroupMatcher[JobKey]) = {
    Future(scheduler.getJobKeys(matcher))(blockingExecutionContext)
  }
}

@Singleton
class GuiceJobFactory @Inject() (injector: Injector) extends JobFactory {
  final val log = Logger(getClass)

  @throws[SchedulerException]
  def newJob(bundle: TriggerFiredBundle, Scheduler: Scheduler): Job = {

    val jobDetail = bundle.getJobDetail
    val jobClass = jobDetail.getJobClass

    try {
      if (log.isDebugEnabled) log.debug("Producing instance of Job '" + jobDetail.getKey + "', class=" + jobClass.getName)
      injector.instanceOf(jobClass)

    } catch {
      case e: Exception => {
        throw new SchedulerException("Problem instantiating class '" + jobDetail.getJobClass.getName + "'", e)
      }
    }
  }
}

