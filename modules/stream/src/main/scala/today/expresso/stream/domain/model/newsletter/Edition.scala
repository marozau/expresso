package today.expresso.stream.domain.model.newsletter

import java.net.URL
import java.time.{Instant, LocalDate}

import play.api.libs.json.JsValue

/**
  * @author im.
  */

case class Edition(id: Long,
                   newsletterId: Long,
                   date: LocalDate,
                   url: Option[URL] = None,
                   title: Option[String],
                   header: Option[JsValue],
                   footer: Option[JsValue],
                   options: Option[JsValue],
                   createdTimestamp: Instant)