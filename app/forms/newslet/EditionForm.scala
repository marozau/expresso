package forms.newslet

import play.api.data.Forms._
import play.api.data._

/**
  * @author im.
  */
object EditionForm {

  val titleForm = Form(
    mapping(
      "id" -> longNumber,
      "text" -> nonEmptyText
    )(TitleData.apply)(TitleData.unapply)
  )

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

  case class TitleData(id: Long, text: String)

  case class HeaderData(id: Long, text: String)

  case class FooterData(id: Long, text: String)
}
