package forms.newslet

import java.net.URL
import java.time.LocalDate

import play.api.data.Forms._
import play.api.data._

/**
  * @author im.
  */
object EditionForm {

  val dateForm = Form(
    mapping(
      "id" -> longNumber,
      "date" -> localDate,
    )(DateData.apply)(DateData.unapply)
  )

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

  import utils.UrlUtils._
  val urlForm = Form(
    mapping(
      "id" -> longNumber,
      "url" -> optional(of[URL])
    )(UrlData.apply)(UrlData.unapply)
  )

  case class DateData(id: Long, date: LocalDate)

  case class TitleData(id: Long, text: String)

  case class HeaderData(id: Long, text: String)

  case class FooterData(id: Long, text: String)

  case class UrlData(id: Long, url: Option[URL])
}
