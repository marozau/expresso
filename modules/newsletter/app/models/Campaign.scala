package models

import java.time.Instant

import play.api.libs.json.JsValue
import today.expresso.stream.api.ToEvent
import today.expresso.stream.domain.event.newsletter.{CampaignStarted, CampaignUpdated}

/**
  * @author im.
  */
object Campaign {

  object Status extends Enumeration {
    type Status = Value
    val NEW,
    PENDING,
    SENDING,
    SENT,
    SUSPENDED_PENDING,
    SUSPENDED_SENDING,
    FORCED_NEW_PENDING,
    FORCED_SUSPENDED_PENDING,
    FORCED_SUSPENDED_SENDING
    = Value
  }


  implicit object ToCampaignStarted extends ToEvent[Campaign, CampaignStarted] {
    override def apply(t: Campaign) = CampaignStarted(t.editionId, t.userId, t.newsletterId, t.sendTime.toEpochMilli)
  }

  implicit object ToCampaignUpdated extends ToEvent[Campaign, CampaignUpdated] {
    override def apply(t: Campaign) = CampaignUpdated(
      t.editionId, t.userId, t.newsletterId, t.sendTime.toEpochMilli, t.status.toString, t.preview, t.options.map(_.toString))
  }
}

case class Campaign(userId: Long,
                    editionId: Long,
                    newsletterId: Long,
                    sendTime: Instant,
                    status: Campaign.Status.Value,
                    preview: Option[String],
                    options: Option[JsValue])