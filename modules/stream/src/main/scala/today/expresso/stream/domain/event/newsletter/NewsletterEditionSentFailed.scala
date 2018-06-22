package today.expresso.stream.domain.event.newsletter

import com.sksamuel.avro4s._
import today.expresso.stream.api.Key
import today.expresso.stream.domain.{Event, Serializer}
import today.expresso.stream.serde.utils.SpecificAvroUtils


/**
  * @author im.
  */
@AvroDoc("newsletter edition was sent to user id")
case class NewsletterEditionSentFailed(@AvroDoc("Key") @Key userId: Long,
                                       editionId: Long,
                                       newsletterId: Long,
                                       attempts: Int,
                                       reason: Option[String])
  extends Event

object NewsletterEditionSentFailed extends Serializer[NewsletterEditionSentFailed] {
  override def toBinary(t: NewsletterEditionSentFailed) = SpecificAvroUtils.serialize[NewsletterEditionSentFailed](t)

  override def fromBinary(bytes: Array[Byte]) = SpecificAvroUtils.deserialize[NewsletterEditionSentFailed](bytes)
}


