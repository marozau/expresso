package today.expresso.stream.domain.event.newsletter

import com.sksamuel.avro4s.AvroDoc
import today.expresso.stream.api.Key
import today.expresso.stream.domain.model.newsletter.Campaign
import today.expresso.stream.domain.{Event, Serializer}
import today.expresso.stream.serde.utils.SpecificAvroUtils

/**
  * @author im.
  */
case class CampaignStarted(@AvroDoc("Key") @Key editionId: Long, campaign: Campaign) extends Event

object CampaignStarted extends Serializer[CampaignStarted] {
  import today.expresso.stream.avro.JsValueMapping._
  import today.expresso.stream.avro.InstantMapping._
  override def toBinary(t: CampaignStarted) = SpecificAvroUtils.serialize[CampaignStarted](t)
  override def fromBinary(bytes: Array[Byte]) = SpecificAvroUtils.deserialize[CampaignStarted](bytes)

  def apply(c: Campaign): CampaignStarted = CampaignStarted(c.editionId, c)
}