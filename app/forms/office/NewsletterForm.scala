package forms.office

import play.api.data.Forms._
import play.api.data._

/**
  * @author im.
  */
object NewsletterForm {

  val headerForm = Form(
    mapping(
      "id" -> longNumber,
      "text" -> nonEmptyText
    )(HeaderData.apply)(HeaderData.unapply)
  )

  val footerForm = Form(
    mapping(
      "id" -> longNumber,
      "text" -> nonEmptyText
    )(FooterData.apply)(FooterData.unapply)
  )

  case class HeaderData(id: Long, text: String)

  case class FooterData(id: Long, text: String)
}
