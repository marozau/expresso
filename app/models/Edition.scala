package models

import java.time.ZonedDateTime

import play.api.libs.json.JsValue

/**
  * @author im.
  */
object Edition {
  //  def draft(newsletterId: Long, postIds: List[Long]) = Edition(None, newsletterId, None, None, None, postIds)
}

case class Edition(id: Option[Long],
                   newsletter: Newsletter,
                   title: Option[String] = None,
                   header: Option[String] = None,
                   footer: Option[String] = None,
                   posts: List[Post] = List.empty,
                   options: Option[JsValue] = None,
                   publishTimestamp: Option[ZonedDateTime] = None)
