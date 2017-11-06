package models

import java.time.ZonedDateTime

import play.api.libs.json.JsValue

/**
  * @author im.
  */

case class Edition(id: Option[Long],
                   newsletterId: Long,
                   title: Option[String] = None,
                   header: Option[String] = None,
                   footer: Option[String] = None,
                   posts: List[Post] = List.empty,
                   options: Option[JsValue] = None,
                   publishTimestamp: Option[ZonedDateTime] = None)
