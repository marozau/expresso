package forms.newslet

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

  def campaignDraft(editionId: Long, newsletterId: Long) = Data(None, newsletterId, editionId, None, ScheduleForm.defaultScheduleTime)
}
