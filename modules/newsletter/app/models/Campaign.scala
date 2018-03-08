package models

import java.time.Instant

import play.api.libs.json.JsValue

/**
  * @author im.
  */
object Campaign {

  object Status extends Enumeration {
    type Status = Value
    val NEW, PENDING, SENDING, SENT, SUSPENDED_PENDING, SUSPENDED_SENDING = Value
  }

}

case class Campaign(editionId: Long,
                    newsletterId: Long,
                    sendTime: Instant,
                    status: Campaign.Status.Value,
                    preview: Option[String],
                    options: Option[JsValue])
