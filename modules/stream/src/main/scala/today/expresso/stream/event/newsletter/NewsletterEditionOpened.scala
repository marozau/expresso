package today.expresso.stream.event.newsletter

import com.sksamuel.avro4s._
import today.expresso.stream.api.Key
import today.expresso.stream.event.Event


/**
  * @author im.
  */
@AvroDoc("newsletter edition was opened by user with userId")
case class NewsletterEditionOpened(@AvroDoc("Key") @Key userId: Long,
                                   editionId: Long,
                                   newsletterId: Long,
                                   timestamp: Long) extends Event
