package models

import java.time.ZonedDateTime

import play.api.libs.json.JsValue

/**
  * @author im.
  */
object Campaign {

  object Status extends Enumeration {
    type Status = Value
    val NEW, PENDING, SENDING, SENT = Value
  }

}

// TODO: https://github.com/typesafehub/lightbend-emoji
case class Campaign(id: Option[Long],
                    editionId: Long,
                    preview: Option[String],
                    sendTime: ZonedDateTime,
                    status: Campaign.Status.Value = Campaign.Status.NEW,
                    options: Option[JsValue] = None)
