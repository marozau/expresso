package forms.office

import play.api.data.Form
import play.api.data.Forms._

/**
  * @author im.
  */
object CampaignForm {

  case class Data(id: Option[Long],
                  newsletterId: Long,
                  name: String,
                  subject: String, //TODO: emoji
                  preview: Option[String], //TODO: emoji
                  fromName: String,
                  fromEmail: String,
                  recipients: Long,
                  sendTime: ScheduleForm.Data)

  val form = Form(
    mapping(
      "id" -> optional(longNumber),
      "newsletterId" -> longNumber,
      "name" -> nonEmptyText,
      "subject" -> nonEmptyText,
      "preview" -> optional(text),
      "fromName" -> nonEmptyText,
      "fromEmail" -> email,
      "recipient" -> longNumber,
      "schedule" -> ScheduleForm.form.mapping
    )(Data.apply)(Data.unapply)
  )

  //TODO: get fromName and fromEmail from user profile
  def campaignDraft(newsletterId: Long, recipientId: Long) = Data(None, newsletterId, "", "", None, "Expresso.today", "hi@expresso.today", recipientId, ScheduleForm.defaultScheduleTime)
}
