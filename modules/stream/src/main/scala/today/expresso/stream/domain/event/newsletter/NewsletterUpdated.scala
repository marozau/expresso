package today.expresso.stream.domain.event.newsletter

import com.sksamuel.avro4s.AvroDoc
import today.expresso.stream.api.Key
import today.expresso.stream.domain.model.newsletter.Newsletter
import today.expresso.stream.domain.{Event, Serializer}
import today.expresso.stream.serde.utils.SpecificAvroUtils

/**
  * @author im.
  */
case class NewsletterUpdated(@AvroDoc("Key") @Key id: Long, newsletter: Newsletter) extends Event

object NewsletterUpdated extends Serializer[NewsletterUpdated] {
  import today.expresso.stream.avro.JsValueMapping._
  import today.expresso.stream.avro.InstantMapping._
  import today.expresso.stream.avro.URLMapping._
  override def toBinary(t: NewsletterUpdated) = SpecificAvroUtils.serialize[NewsletterUpdated](t)
  override def fromBinary(bytes: Array[Byte]) = SpecificAvroUtils.deserialize[NewsletterUpdated](bytes)

  def apply(n: Newsletter): NewsletterUpdated = NewsletterUpdated(n.id, n)
}
