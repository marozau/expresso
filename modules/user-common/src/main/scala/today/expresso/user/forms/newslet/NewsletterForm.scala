package forms.newslet

import java.net.URL

import play.api.data.Forms._
import play.api.data._

/**
  * @author im.
  */
object NewsletterForm {

  import today.expresso.common.utils.UrlUtils._

  val form = Form(
    mapping(
      "name" -> nonEmptyText,
      "nameUrl" -> nonEmptyText,
      "email" -> email,
      "lang" -> nonEmptyText,
      "logo" -> optional(of[URL])
    )(Data.apply)(Data.unapply)
  )

  case class Data(name: String, nameUrl: String, email: String, lang: String, logo: Option[URL])
}
