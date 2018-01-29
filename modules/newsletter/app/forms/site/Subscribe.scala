package forms.site

import play.api.data.Form
import play.api.data.Forms._

/**
  * @author im.
  */
object Subscribe {

  val form = Form(
    mapping(
      "listId" -> text,
      "email" -> optional(email)
    )(Data.apply)(Data.unapply)
  )

  case class Data(listId: String, email: Option[String])

  def empty(listId: String) = form.fill(Data(listId, None))
}
