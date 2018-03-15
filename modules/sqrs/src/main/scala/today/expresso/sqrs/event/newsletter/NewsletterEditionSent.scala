package today.expresso.sqrs.event.newsletter

import com.sksamuel.avro4s.{AvroDoc, AvroSchema, RecordFormat, SchemaFor}
import org.apache.avro.Schema


/**
  * @author im.
  */
@AvroDoc("newsletter edition was sent to user id")
case class NewsletterEditionSent(editionId: Long,
                                 newsletterId: Long,
                                 userId: Long,
                                 timestamp: Long)

object NewsletterEditionSent {
  implicit val schemaFor = SchemaFor[NewsletterEditionSent]
  val schema = AvroSchema[NewsletterEditionSent]
  val format = RecordFormat[NewsletterEditionSent]
}