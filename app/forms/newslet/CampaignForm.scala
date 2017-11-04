package forms.newslet

import play.api.data.Form
import play.api.data.Forms._

/**
  * @author im.
  */
object CampaignForm {

  case class Data(id: Option[Long],
                  editionId: Long,
                  preview: Option[String], //TODO: emoji
                  sendTime: ScheduleForm.Data)

  val form = Form(
    mapping(
      "id" -> optional(longNumber),
      "editionId" -> longNumber,
      "preview" -> optional(text),
      "schedule" -> ScheduleForm.form.mapping
    )(Data.apply)(Data.unapply)
  )

  //TODO: get fromName and fromEmail from user profile
  def campaignDraft(editionId: Long) = Data(None, editionId, None, ScheduleForm.defaultScheduleTime)
}
