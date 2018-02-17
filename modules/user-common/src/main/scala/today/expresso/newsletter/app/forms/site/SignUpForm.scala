package forms.site

import play.api.data.Form
import play.api.data.Forms._

/**
  * @author im.
  */
object SignUpForm {

  val form = Form(
    mapping(
      "newsletterId" -> longNumber,
      "email" -> optional(email)
    )(Data.apply)(Data.unapply)
  )

  case class Data(newsletterId: Long, email: Option[String])

  def empty(newsletterId: Long) = form.fill(Data(newsletterId, None))
}
