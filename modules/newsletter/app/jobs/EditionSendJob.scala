package jobs

import java.lang.invoke.MethodHandles
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.inject.Inject

import clients.Mailer.EmailHtml
import clients.{Mailer, Quartz}
import models.{Campaign, Target}
import org.quartz.core.jmx.JobDataMapSupport
import org.quartz._
import org.slf4j.LoggerFactory
import play.api.i18n._
import services.{CompilerService, EditionService, NewsletterService, UserService}
import _root_.utils.UrlUtils

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
object EditionSendJob {

  def group = "edition"

  def edition(campaignId: Long) = s"$group-$campaignId"

  def identity(userId: Long, campaign: Campaign) = s"${edition(campaign.editionId)}-$userId"


  def buildTrigger(userId: Long, campaign: Campaign): Trigger = {
    // Prefef.long2Long is nedded to avoid error: the result type of an implicit conversion must be more specific than AnyRef
    val jobDataMap = Map[String, AnyRef](
      "userId" -> Predef.long2Long(userId),
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
                              (implicit ec: ExecutionContext)
  extends RecoveringJob(quartz) {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  val DEFAULT_LOCALE = "ru"

  override protected def execute(context: JobExecutionContext, retry: Int): Unit = {
    val data = context.getMergedJobDataMap
    val userId = data.get("userId").asInstanceOf[Long]
    val editionId = data.get("editionId").asInstanceOf[Long]
    val newsletterId = data.get("newsletterId").asInstanceOf[Long]
    logger.info(s"execute EditionSendJob: userId=$userId, newsletterId=$newsletterId, editionId=$editionId")
//    editionService.getById(editionId)
//      .flatMap(edition => ph.doEdition(edition, Target.EMAIL))
//      .flatMap(edition => userService.getById(userId).map((_, edition)))
//      .map { case (user, edition) =>
//          implicit val requestHeader = urlUtils.mockRequestHeader
//          implicit val messages: Messages = MessagesImpl(edition.newsletter.locale, messagesApi)
//          val newsletterBody = views.html.email.newsletter(edition).body
//
//          val email = EmailHtml(
//            editionId,
//            userId,
//            edition.title.getOrElse(edition.newsletter.name),
//            Seq(user.email),
//            edition.newsletter.name,
//            "reply_here@expresso.today", //TODO: centralized email answer service. Possibly its better to integrate with Slack
//            newsletterBody
//          )
//          mailer.send(email).map(Some(_))
//      }

    //    quartz.scheduler.pauseTriggers(new GroupMatcher[TriggerKey]("asdf", StringMatcher.StringOperatorName.STARTS_WITH))
  }
}
