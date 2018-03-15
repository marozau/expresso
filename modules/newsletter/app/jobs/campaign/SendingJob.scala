package jobs.campaign

import java.lang.invoke.MethodHandles
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.inject.Inject

import clients.Mailer.EmailHtml
import clients.Quartz
import jobs.api.RecoveringJob
import models.Campaign
import org.quartz._
import org.slf4j.LoggerFactory
import play.api.i18n._
import services._
import today.expresso.common.exceptions.RecipientNotFoundException
import today.expresso.common.utils.UrlUtils
import today.expresso.templates.api.domain.{Edition, Newsletter, Target}
import today.expresso.templates.impl.TemplateService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

/**
  * @author im.
  */
object SendingJob {

  def name(userId: Long, c: Campaign) = s"${CampaignJob.identity(c)}-$userId"

  def buildJobData(userId: Long, c: Campaign) = {
    // Prefef.long2Long is nedded to avoid error: the result type of an implicit conversion must be more specific than AnyRef
    val jobDataMap = Map[String, AnyRef](
      "userId" -> Predef.long2Long(userId),
      "editionId" -> Predef.long2Long(c.editionId),
      "newsletterId" -> Predef.long2Long(c.newsletterId),
      "sendTime" -> Predef.long2Long(c.sendTime.toEpochMilli),
      "status" -> c.status.toString
    )
    Quartz.newJobDataMap(jobDataMap)
  }

  def buildTrigger(userId: Long, c: Campaign): Trigger = {

    val jobData = buildJobData(userId, c)

    TriggerBuilder.newTrigger()
      .withIdentity(name(userId, c), CampaignJob.identity(c))
      .usingJobData(jobData)
      .startAt(Date.from(Instant.now().plus(1, ChronoUnit.MINUTES)))
      .build()
  }

  def buildJob(userId: Long, campaign: Campaign): JobDetail = {
    JobBuilder.newJob(classOf[SendingJob])
      .withIdentity(name(userId, campaign), CampaignJob.userGroup(userId))
      .requestRecovery
      .build
  }
}

class SendingJob @Inject()(quartz: Quartz,
                           userService: UserService,
                           editionService: EditionService,
                           newsletterService: NewsletterService,
                           templateService: TemplateService,
                           campaignRecipientService: CampaignRecipientService,
                           mailService: MailService,
                           urlUtils: UrlUtils,
                           langs: Langs,
                           messagesApi: MessagesApi)
                          (implicit ec: ExecutionContext)
  extends RecoveringJob(quartz) {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  override protected def execute(context: JobExecutionContext, retry: Int): Unit = {
    val data = context.getMergedJobDataMap
    val userId = data.get("userId").asInstanceOf[Long]
    val editionId = data.get("editionId").asInstanceOf[Long]
    val newsletterId = data.get("newsletterId").asInstanceOf[Long]
    val status = Campaign.Status.withName(data.get("status").asInstanceOf[String])
    val sendTime = Instant.ofEpochMilli(data.get("sendTime").asInstanceOf[Long])
    val campaign = Campaign(userId, editionId, newsletterId, sendTime, status, None, None)
    logger.info(s"execute, userId=$userId, newsletterId=$newsletterId, editionId=$editionId")
    val sending = (for {
      user <- userService.getById(userId)
      newsletter <- newsletterService.getById(userId, newsletterId)
      edition <- editionService.getById(userId, editionId)
    } yield (user, newsletter, edition))
      .flatMap { case (user, newsletter, edition) =>
        //TODO: validate edition before scheduling
        val templateEdition = Edition(
          edition.id,
          edition.url.map(_.toString).get,
          Newsletter(newsletter.id, newsletter.userId, newsletter.name, Lang(newsletter.locale.toString), newsletter.logoUrl, newsletter.avatarUrl, newsletter.options),
          edition.date,
          edition.title.get,
          List.empty,
          edition.header,
          edition.footer,
          edition.options
        )
        templateService.getNewsletterTemplate(templateEdition, Target.EMAIL)
          .map((user, newsletter, edition, _))
      }
      .flatMap { case (user, newsletter, edition, template) =>
        val email = EmailHtml(
          editionId,
          userId,
          edition.title.getOrElse(newsletter.name),
          Seq(user.email),
          newsletter.name,
          "reply_here@expresso.today", //TODO: centralized email answer service. Possibly its better to integrate with Slack
          template.body
        )
        mailService.send(email)
      }
      .flatMap { _ =>
        campaignRecipientService.markSent(userId, editionId)
      }
      .recover {
        case e: RecipientNotFoundException =>
          logger.warn(s"failed to send email, user=${userId}, editionId=${editionId}", e)
        case t: Throwable =>
          logger.error(s"failed to send email, user=${userId}, editionId=${editionId}", t)
          campaignRecipientService.markFailed(userId, editionId, Some(t.getMessage)).map(_ => throw t)
      }
    Await.result(sending, Duration.Inf)
    logger.info("complete")
  }
}