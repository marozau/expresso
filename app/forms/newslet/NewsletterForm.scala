package forms.newslet

import play.api.data.Forms._
import play.api.data._

/**
  * @author im.
  */
object NewsletterForm {

  val form = Form(
    mapping(
      "name" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  case class Data(name: String)
}
