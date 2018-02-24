package today.expresso.templates.api.domain

import java.time.{Instant, LocalDate}

import play.api.libs.json.JsValue

/**
  * @author im.
  */

case class Edition(id: Long,
                   url: String,
                   newsletter: Newsletter,
                   date: LocalDate,
                   title: String,
                   posts: List[Post],
                   header: Option[JsValue],
                   footer: Option[JsValue],
                   options: Option[JsValue],
                   createdTimestamp: Instant)