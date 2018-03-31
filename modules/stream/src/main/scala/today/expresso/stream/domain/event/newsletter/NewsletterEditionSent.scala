package today.expresso.stream.domain.event.newsletter

import com.sksamuel.avro4s._
import today.expresso.stream.api.Key
import today.expresso.stream.domain.Event


/**
  * @author im.
  */
@AvroDoc("newsletter edition was sent to user id")
case class NewsletterEditionSent(@AvroDoc("Key") @Key userId: Long,
                                 editionId: Long,
                                 newsletterId: Long,
                                 timestamp: Long) extends Event
