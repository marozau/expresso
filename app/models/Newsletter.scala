package models

import java.time.ZonedDateTime

import play.api.libs.json.JsValue

/**
  * @author im.
  */
object Newsletter {
  def draft(userId: Long, postIds: List[Long]) = Newsletter(None, userId, None, None, None, None, postIds)
}
case class Newsletter(id: Option[Long],
                      userId: Long,
                      url: Option[String],
                      title: Option[String],
                      header: Option[String],
                      footer: Option[String],
                      postIds: List[Long],
                      options: Option[JsValue] = None,
                      publishTimestamp: Option[ZonedDateTime] = None,
                      createdTimestamp: Option[ZonedDateTime] = None,
                      modifiedTimestamp: Option[ZonedDateTime] = None)
