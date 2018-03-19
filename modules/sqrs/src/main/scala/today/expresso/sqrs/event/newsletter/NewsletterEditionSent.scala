package today.expresso.sqrs.event.newsletter

import com.sksamuel.avro4s._
import today.expresso.sqrs.api.Key


/**
  * @author im.
  */
@AvroDoc("newsletter edition was sent to user id")
case class NewsletterEditionSent(@AvroDoc("Key") @Key userId: Long,
                                 editionId: Long,
                                 newsletterId: Long,
                                 timestamp: Long)
