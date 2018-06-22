package today.expresso.stream.domain.event.newsletter

import com.sksamuel.avro4s._
import today.expresso.stream.api.Key
import today.expresso.stream.domain.{Event, Serializer}
import today.expresso.stream.serde.utils.SpecificAvroUtils


/**
  * @author im.
  */
//TODO: looks like this event must be in the tracking system not newsletter

@AvroDoc("newsletter edition was opened by user with userId")
case class NewsletterEditionOpened(@AvroDoc("Key") @Key userId: Long,
                                   editionId: Long,
                                   newsletterId: Long,
                                   timestamp: Long)
  extends Event

object NewsletterEditionOpened extends Serializer[NewsletterEditionOpened] {
  override def toBinary(t: NewsletterEditionOpened) = SpecificAvroUtils.serialize[NewsletterEditionOpened](t)
  override def fromBinary(bytes: Array[Byte]) = SpecificAvroUtils.deserialize[NewsletterEditionOpened](bytes)
}
