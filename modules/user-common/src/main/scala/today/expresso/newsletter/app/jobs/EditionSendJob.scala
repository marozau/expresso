package jobs

import java.lang.invoke.MethodHandles
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.inject.Inject

import clients.Mailer.EmailHtml
import clients.{Mailer, Quartz}
import controllers.AssetsFinder
import models.{Campaign, Target}
import org.quartz.core.jmx.JobDataMapSupport
import org.quartz._
import org.slf4j.LoggerFactory
import play.api.i18n._
import services.{CompilerService, EditionService, NewsletterService, UserService}
import today.expresso.common.utils.UrlUtils

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
object EditionSendJob {

  def group = "edition"

  def edition(campaignId: Long) = s"$group-$campaignId"

  def identity(userId: Long, campaign: Campaign) = s"${edition(campaign.id.get)}-$userId"


  def buildTrigger(userId: Long, campaign: Campaign): Trigger = {
    // Prefef.long2Long is nedded to avoid error: the result type of an implicit conversion must be more specific than AnyRef
    val jobDataMap = Map[String, AnyRef](
      "userId" -> Predef.long2Long(userId),
      "campaignId" -> Predef.long2Long(campaign.id.get),
      "editionId" -> Predef.long2Long(campaign.editionId),
      "newsletterId" -> Predef.long2Long(campaign.newsletterId),
    )
    import scala.collection.JavaConverters._
    val jobData = JobDataMapSupport.newJobDataMap(jobDataMap.asJava)

    TriggerBuilder.newTrigger()
      .withIdentity(identity(userId, campaign), group)
      .usingJobData(jobData)
      .startAt(Date.from(Instant.now().plus(1, ChronoUnit.MINUTES)))
      .build()
  }

  def buildJob(userId: Long, campaign: Campaign): JobDetail = {
    JobBuilder.newJob(classOf[EditionSendJob])
      .withIdentity(identity(userId, campaign), group)
      .requestRecovery
      .build
  }
}

class EditionSendJob @Inject()(quartz: Quartz,
                               userService: UserService,
                               newsletterService: NewsletterService,
                               editionService: EditionService,
                               mailer: Mailer,
                               ph: CompilerService,
                               urlUtils: UrlUtils,
                               langs: Langs,
                               messagesApi: MessagesApi)
                              (implicit
                               ec: ExecutionContext,
                               assets: AssetsFinder)
  extends RecoveringJob(quartz) {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  val DEFAULT_LOCALE = "ru"

  override protected def execute(context: JobExecutionContext, retry: Int): Unit = {
    val data = context.getMergedJobDataMap
    val userId = data.get("userId").asInstanceOf[Long]
    val campaignId = data.get("campaignId").asInstanceOf[Long]
    val editionId = data.get("editionId").asInstanceOf[Long]
    val newsletterId = data.get("newsletterId").asInstanceOf[Long]
    logger.info(s"execute EditionSendJob: userId=$userId, campaignId=$campaignId, newsletterId=$newsletterId, editionId=$editionId")
    editionService.getById(editionId)
      .flatMap(edition => ph.doEdition(edition, Target.EMAIL))
      .flatMap(edition => userService.retrieve(userId).map((_, edition)))
      .map { case (user, edition) =>
        if (user.isEmpty) {
          logger.error(s"failed to send edition, user not found, userId=$userId, editionId=$editionId")
          None
        } else {
          implicit val requestHeader = urlUtils.mockRequestHeader
          implicit val messages: Messages = MessagesImpl(edition.newsletter.lang, messagesApi)
          val newsletterBody = views.html.email.newsletter(edition).body

          val email = EmailHtml(
            campaignId,
            userId,
            edition.title.getOrElse(edition.newsletter.name),
            Seq(user.get.email),
            edition.newsletter.name,
            edition.newsletter.email,
            newsletterBody
          )
          mailer.send(email).map(Some(_))
        }
      }

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
