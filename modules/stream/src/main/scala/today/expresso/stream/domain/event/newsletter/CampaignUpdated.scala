package today.expresso.stream.domain.event.newsletter

import com.sksamuel.avro4s.AvroDoc
import today.expresso.stream.api.Key

/**
  * @author im.
  */
case class CampaignUpdated(@AvroDoc("Key") @Key editionId: Long,
                           userId: Long,
                           newsletterId: Long,
                           sendTime: Long,
                           status: String,
                           preview: Option[String],
                           options: Option[String])
