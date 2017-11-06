package forms.newslet

import play.api.data.Forms._
import play.api.data._

/**
  * @author im.
  */
object RecipientForm {

  val form = Form(
    mapping(
      "newsletterId" -> longNumber,
      "userId" -> optional(longNumber)
    )(Data.apply)(Data.unapply)
  )

  case class Data(newsletterId: Long, userId: Option[Long])

  def empty(newsletterId: Long) = form.fill(Data(newsletterId, None))
}
