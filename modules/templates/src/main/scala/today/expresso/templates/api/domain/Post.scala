package today.expresso.templates.api.domain

import play.api.libs.json.JsValue

/**
  * @author im.
  */
case class Post(id: Long,
                url: String,
                userId: Long,
                editionId: Option[Long],
                title: String,
                annotation: String,
                body: String,
                options: Option[JsValue] = None)

