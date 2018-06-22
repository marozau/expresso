package today.expresso.stream.domain.model.newsletter

import java.time.Instant

import play.api.libs.json.JsValue

/**
  * @author im.
  */
// TODO: https://github.com/typesafehub/lightbend-emoji twirl method template
case class Post(id: Long,
                userId: Long,
                editionId: Long,
                editionOrder: Int,
                title: String,
                annotation: String,
                body: JsValue,
                options: Option[JsValue],
                createdTimestamp: Instant)

