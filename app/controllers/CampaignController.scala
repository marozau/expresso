package controllers

import java.time._
import javax.inject.{Inject, Singleton}

import models.Campaign
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.data.Forms._
import play.api.data._
import repositories.{CampaignRepository, NewsletterRepository, RecipientRepository}
import services.JobScheduler

import scala.concurrent.{ExecutionContext, Future}


/**
  * @author im.
  */
object CampaignController {

  case class Time(hour: Int, minute: Int) {
    override def toString: String = s"${if (hour > 9) hour else "0" + hour}:${if (minute > 9) minute else "0" + minute}"

    def toLocal: LocalTime = LocalTime.of(hour, minute)
  }

  object Time {
    def apply(string: String): Time = {
      val t = string.split(":")
      Time(t.head.toInt, t.last.toInt)
    }
  }

  case class ScheduleTime(zoneOffset: Int, date: java.time.LocalDate, time: Time) {
    lazy val toDateTime: ZonedDateTime = ZonedDateTime.of(date, time.toLocal, ZoneOffset.ofHours(zoneOffset))
  }

  val defaultTimeZone = 3
  val defaultTime = Time(6, 30)
  val defaultScheduleTime = ScheduleTime(defaultTimeZone, nearestDate(), defaultTime)

  def nearestDate(): LocalDate = {
    val zoneOffset = ZoneOffset.ofHours(defaultTimeZone)
    val now = Instant.now().atOffset(zoneOffset).toEpochSecond
    val fire = LocalDate.now.atTime(defaultTime.hour, defaultTime.minute).toEpochSecond(zoneOffset)
    if (fire < now) LocalDate.now.plusDays(1) else LocalDate.now
  }


  case class CampaignForm(id: Option[Long],
                          newsletterId: Long,
                          name: String,
                          subject: String, //TODO: emoji
                          preview: Option[String], //TODO: emoji
                          fromName: String,
                          fromEmail: String,
                          recipients: Long,
                          sendTime: ScheduleTime)

  def campaignDraft(newsletterId: Long, recipientId: Long) = CampaignForm(None, newsletterId, "", "", None, "Expresso.today", "hi@expresso.today", recipientId, defaultScheduleTime)

}

@Singleton
class CampaignController @Inject()(cc: ControllerComponents,
                                   campaigns: CampaignRepository,
                                   recipients: RecipientRepository,
                                   jobScheduler: JobScheduler)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  import CampaignController._
  import implicits.CampaignImplicits._
  import utils.TimeUtils._

  val USER_ID = 10000000L

  val scheduleForm = Form(
    mapping(
      "zoneOffset" -> number(min = -12, max = 12),
      "date" -> localDate("yyyy-MM-dd"), //TODO: validation
      "time" -> of[Time]
    )(ScheduleTime.apply)(ScheduleTime.unapply)
  )

  val campaignForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "newsletterId" -> longNumber,
      "name" -> nonEmptyText,
      "subject" -> nonEmptyText,
      "preview" -> optional(text),
      "fromName" -> nonEmptyText,
      "fromEmail" -> email,
      "recipient" -> longNumber,
      "schedule" -> scheduleForm.mapping
    )(CampaignForm.apply)(CampaignForm.unapply)
  )

  def getCampaignForm(id: Option[Long], newsletterId: Option[Long]) = Action.async { implicit request =>
    val recipient = recipients.getByUserId(USER_ID)

    def existing(id: Long) = {
      campaigns.getById(id)
        .map(campaign => campaignForm.fill(campaign))
    }

    def empty() = {
      campaigns.getByNewsletterId(USER_ID, newsletterId.get)
        .flatMap { campaign =>
          if (campaign.isDefined) {
            Future(campaignForm.fill(campaign.get))
          } else {
            recipient.map { r =>
              val defaultList = r.filter(_.default.isDefined).filter(_.default.get)
              val default = if (defaultList.nonEmpty) defaultList.head else r.head
              campaignForm.fill(campaignDraft(newsletterId.get, default.id.get))
            }
          }
        }
    }

    id.fold(empty())(existing)
      .flatMap(f => recipient.map((f, _)))
      .map {
        case (form, rec) => Ok(views.html.admin.campaign(form, rec))
      }
  }

  def submitCampaignForm() = Action.async { implicit request =>
    campaignForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.info(s"bad campaign, form=$formWithErrors")
        Future(BadRequest(formWithErrors.toString))
        //        Future(BadRequest(views.html.admin.campaign(formWithErrors)))
      },
      form => {
        val campaign = Campaign(form.id, USER_ID, form.newsletterId, form.name, form.subject, form.preview, form.fromName, form.fromEmail,
          form.sendTime.toDateTime, form.recipients)
        form.id.fold(campaigns.create(campaign))(_ => campaigns.update(campaign).map(_ => campaign))
          .map(c => Redirect(routes.NewsletterController.getNewsletterFinal(c.id.get, c.newsletterId)))
      }
    )
  }

  def scheduleCampaign(id: Long) = Action.async { implicit request =>
    campaigns.getById(id)
      .flatMap { campaign =>
        jobScheduler.schedule(campaign)
          .map(_ => campaign)
      }
      .map(_ => Ok("Done"))
  }

}
