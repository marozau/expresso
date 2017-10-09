package models

import java.time.ZonedDateTime

import play.api.libs.json.JsValue

/**
  * @author im.
  */
// TODO: https://github.com/typesafehub/lightbend-emoji twirl method template
case class Post(id: Option[Long],
                userId: Long,
                newsletterId: Option[Long],
                title: String,
                annotation: String,
                body: String,
                refs: List[String],
                options: Option[JsValue] = None,
                createdTimestamp: Option[ZonedDateTime] = None,
                modifiedTimestamp: Option[ZonedDateTime] = None)

