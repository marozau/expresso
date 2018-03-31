package today.expresso.stream.domain.event.newsletter

import com.sksamuel.avro4s.AvroDoc
import today.expresso.stream.api.Key
import today.expresso.stream.domain.Event

/**
  * @author im.
  */
case class NewsletterCreated(@AvroDoc("Key") @Key id: Long,
                             userId: Long,
                             name: String,
                             locale: String,
                             createdTimestamp: Long) extends Event
