package clients

import java.util
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
trait Quartz {
  def scheduleJobBlocking(trigger: Trigger): Date

  def rescheduleJobBlocking(triggerKey: TriggerKey, newTrigger: Trigger): Date

  def scheduleUniqueJob(jobDetail: JobDetail, trigger: Trigger): Future[Date]

  def scheduleJob(jobDetail: JobDetail, trigger: Trigger): Future[Date]

  def rescheduleJob(triggerKey: TriggerKey, newTrigger: Trigger): Future[Date]

  def pauseTriggers(matcher: GroupMatcher[TriggerKey]): Future[Unit]

  def deleteJob(jobKey: JobKey): Future[Boolean]

  def checkExists(triggerKey: TriggerKey): Future[Boolean]

  def getJobKeys(matcher: GroupMatcher[JobKey]): Future[util.Set[JobKey]]
}

@Singleton
class QuartzImpl @Inject()(appLifecycle: ApplicationLifecycle,
                           config: Configuration,
                           actorSystem: ActorSystem,
                           jobFactory: JobFactory)(implicit ec: ExecutionContext) extends Quartz {

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

  override def scheduleJobBlocking(trigger: Trigger): Date = {
    scheduler.scheduleJob(trigger)
  }

  override def rescheduleJobBlocking(triggerKey: TriggerKey, newTrigger: Trigger): Date = {
    scheduler.rescheduleJob(triggerKey, newTrigger)
  }

  override def scheduleUniqueJob(jobDetail: JobDetail, trigger: Trigger): Future[Date] = {
    Future(scheduler.scheduleJob(jobDetail, trigger))(blockingExecutionContext)
  }

  override def scheduleJob(jobDetail: JobDetail, trigger: Trigger): Future[Date] = {
    checkExists(trigger.getKey)
      .flatMap { exists =>
        if (exists) rescheduleJob(trigger.getKey, trigger)
        else scheduleUniqueJob(jobDetail, trigger)
      }
  }

  override def rescheduleJob(triggerKey: TriggerKey, newTrigger: Trigger): Future[Date] = {
    Future(scheduler.rescheduleJob(triggerKey, newTrigger))(blockingExecutionContext)
  }

  override def pauseTriggers(matcher: GroupMatcher[TriggerKey]): Future[Unit] = {
    Future(scheduler.pauseTriggers(matcher))(blockingExecutionContext)
  }

  override def deleteJob(jobKey: JobKey): Future[Boolean] = {
    Future(scheduler.deleteJob(jobKey))(blockingExecutionContext)
  }

  override def checkExists(triggerKey: TriggerKey): Future[Boolean] = {
    Future(scheduler.checkExists(triggerKey))(blockingExecutionContext)
  }

  override def getJobKeys(matcher: GroupMatcher[JobKey]): Future[util.Set[JobKey]] = {
    Future(scheduler.getJobKeys(matcher))(blockingExecutionContext)
  }
}

@Singleton
class GuiceJobFactory @Inject()(injector: Injector) extends JobFactory {
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
