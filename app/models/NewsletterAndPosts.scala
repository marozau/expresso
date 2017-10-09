package models

import java.time.ZonedDateTime

import play.api.libs.json.JsValue

/**
  * @author im.
  */
object NewsletterAndPosts {
  def draft(userId: Long, posts: List[Post]) = NewsletterAndPosts(None, userId, None, None, None, None, posts)
}

case class NewsletterAndPosts(id: Option[Long],
                              userId: Long,
                              url: Option[String],
                              title: Option[String],
                              header: Option[String],
                              footer: Option[String],
                              posts: List[Post],
                              options: Option[JsValue] = None,
                              publishTimestamp: Option[ZonedDateTime] = None,
                              createdTimestamp: Option[ZonedDateTime] = None,
                              modifiedTimestamp: Option[ZonedDateTime] = None)
