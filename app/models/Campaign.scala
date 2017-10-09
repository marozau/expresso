package models

import java.time.ZonedDateTime

import play.api.libs.json.JsValue

/**
  * @author im.
  */
object Campaign {

  object Status extends Enumeration {
    type Status = Value
    val NEW, PENDING, SENT = Value
  }

}

// TODO: https://github.com/typesafehub/lightbend-emoji
case class Campaign(id: Option[Long],
                    userId: Long,
                    newsletterId: Long,
                    name: String,
                    subject: String, //TODO: emoji
                    preview: Option[String], //TODO: emoji
                    fromName: String,
                    fromEmail: String,
                    sendTime: ZonedDateTime,
                    recipientId: Long,
                    status: Campaign.Status.Value = Campaign.Status.NEW,
                    emailsSent: Int = 0,
                    options: Option[JsValue] = None,
                    createdTimestamp: Option[ZonedDateTime] = None,
                    modifiedTimestamp: Option[ZonedDateTime] = None)
