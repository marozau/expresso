package models

import java.net.URL
import java.time.LocalDate

import play.api.libs.json.JsValue

/**
  * @author im.
  */

case class Edition(id: Option[Long],
                   newsletter: Newsletter,
                   date: LocalDate,
                   url: Option[URL] = None,
                   title: Option[String] = None,
                   header: Option[String] = None,
                   footer: Option[String] = None,
                   posts: List[Post] = List.empty,
                   options: Option[JsValue] = None)