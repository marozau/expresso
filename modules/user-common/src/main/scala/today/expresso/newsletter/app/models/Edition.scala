package models

import java.net.URL
import java.time.Instant
import java.util.Date

import play.api.libs.json.JsValue

/**
  * @author im.
  */

case class Edition(id: Long,
                   newsletterId: Long,
                   date: Date,
                   url: Option[URL] = None,
                   title: Option[String],
                   header: Option[JsValue],
                   footer: Option[JsValue],
                   options: Option[JsValue],
                   createdTimestamp: Instant)