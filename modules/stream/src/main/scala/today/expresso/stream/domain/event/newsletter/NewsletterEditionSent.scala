package today.expresso.stream.domain.event.newsletter

import com.sksamuel.avro4s._
import today.expresso.stream.api.Key
import today.expresso.stream.domain.{Event, Serializer}
import today.expresso.stream.serde.utils.SpecificAvroUtils


/**
  * @author im.
  */
@AvroDoc("newsletter edition was sent to user id")
case class NewsletterEditionSent(@AvroDoc("Key") @Key userId: Long,
                                 editionId: Long,
                                 newsletterId: Long,
                                 timestamp: Long)
  extends Event

object NewsletterEditionSent extends Serializer[NewsletterEditionSent] {
  override def toBinary(t: NewsletterEditionSent) = SpecificAvroUtils.serialize[NewsletterEditionSent](t)
  override def fromBinary(bytes: Array[Byte]) = SpecificAvroUtils.deserialize[NewsletterEditionSent](bytes)
}
