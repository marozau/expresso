package forms.office

import java.net.URL

import models.Post
import play.api.data.Form
import play.api.data.Forms._

/**
  * @author im.
  */
object PostForm {
  import implicits.PostImplicits._
  import utils.UrlUtils._

  val form = Form(
    mapping(
      "id" -> optional(longNumber),
      "newsletterId" -> optional(longNumber),
      "title" -> nonEmptyText,
      "annotation" -> nonEmptyText,
      "body" -> nonEmptyText,
      "refs" -> list(of[URL])
    )(Data.apply)(Data.unapply)
  )

  case class Data(id: Option[Long],
                      newsletterId: Option[Long],
                      title: String,
                      annotation: String,
                      body: String,
                      refs: List[URL])

  implicit def postFormCast(p: Post): PostForm.Data = Data(p.id, p.newsletterId, p.title, p.annotation, p.body, p.refs)
}
