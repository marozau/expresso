package forms.newslet

import models.{Edition, User}
import play.api.data.Form
import play.api.data.Forms._

/**
  * @author im.
  */
object CampaignForm {

  case class Data(id: Option[Long],
                  newsletterId: Long,
                  editionId: Long,
                  preview: Option[String], //TODO: emoji
                  sendTime: ScheduleForm.Data)

  val form = Form(
    mapping(
      "id" -> optional(longNumber),
      "newsletterId" -> longNumber,
      "editionId" -> longNumber,
      "preview" -> optional(text),
      "schedule" -> ScheduleForm.form.mapping
    )(Data.apply)(Data.unapply)
  )

  implicit def campaignDraft(user: User, edition: Edition): Data = {
    Data(
      None,
      edition.newsletter.id.get,
      edition.id.get,
      None,
      ScheduleForm.defaultScheduleTime(user.timezone.getOrElse(0), edition.date))
  }
}
