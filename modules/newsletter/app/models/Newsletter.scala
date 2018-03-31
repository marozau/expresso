package models

import java.net.URL
import java.time.Instant

import play.api.libs.json.JsValue
import today.expresso.stream.api.ToEvent
import today.expresso.stream.domain.event.newsletter.{NewsletterCreated, NewsletterUpdated}

/**
  * @author im.
  */
//TODO: nameUrl will be used in archive component

case class Newsletter(id: Long,
                      userId: Long,
                      name: String,
                      locale: Locale.Value,
                      logoUrl: Option[URL],
                      avatarUrl: Option[URL],
                      options: Option[JsValue],
                      createdTimestamp: Instant)


object Newsletter {

  implicit object ToNewsletterCreated extends ToEvent[Newsletter, NewsletterCreated] {
    override def apply(t: Newsletter) =
      NewsletterCreated(
        t.id,
        t.userId,
        t.name,
        t.locale.toString,
        t.createdTimestamp.toEpochMilli)
  }

  implicit object ToNewsletterUpdated extends ToEvent[Newsletter, NewsletterUpdated] {
    override def apply(t: Newsletter) =
      NewsletterUpdated(
        t.id,
        t.userId,
        t.name,
        t.locale.toString,
        t.logoUrl.map(_.toString),
        t.avatarUrl.map(_.toString),
        t.options.map(_.toString)
      )
  }

}