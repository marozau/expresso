package today.expresso.stream.domain.model.newsletter

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