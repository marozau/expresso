package forms.newslet

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
      "editionId" -> optional(longNumber),
      "title" -> nonEmptyText,
      "annotation" -> nonEmptyText,
      "body" -> nonEmptyText,
      "refs" -> list(of[URL])
    )(Data.apply)(Data.unapply)
  )

  case class Data(id: Option[Long],
                  editionId: Option[Long],
                  title: String,
                  annotation: String,
                  body: String,
                  refs: List[URL])

  implicit def postFormCast(p: Post): PostForm.Data = Data(p.id, p.editionId, p.title, p.annotation, p.body, p.refs)
}
