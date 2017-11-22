package events.newsletter

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.google.common.io.BaseEncoding
import com.sksamuel.avro4s._
import events.Tracking
import play.api.mvc.QueryStringBindable

/**
  * @author im.
  */
@AvroNamespace("today.expresso.newslet")
case class Subscribe(
                      @AvroDoc("recipients uuid least significant bits") recipientIdMostSigBits: Long,
                      @AvroDoc("recipients uuid most significant bits") recipientIdLeastSigBits: Long,
                      @AvroDoc("users table id") userId: Long,
                      @AvroDoc("newsletter table id") newsletterId: Long,
                      @AvroDoc("server side event id") eventId: Option[Long] = None,
                      @AvroDoc("server side event timestamp") timestamp: Option[Long] = None,
                      @AvroDoc("client's ip address, can be a proxy server address") ip: Option[String] = None,
                      useragent: Option[String] = None) extends Tracking

object Subscribe {
  implicit val schemaFor = SchemaFor[Subscribe]
  val format = RecordFormat[Subscribe]

  // TODO: replace by confluent serializer with schema registry
  // TODO: fallback strategy in case when schema registry or other 3rd party is not available
  // TODO: fallback strategy, read first bytes manually
  implicit def queryStringBindable(implicit stringBinder: QueryStringBindable[String]) = new QueryStringBindable[Subscribe] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Subscribe]] = {
      for {
        data <- stringBinder.bind("data", params)
      } yield {
        data match {
          case (Right(from)) =>
            val bytes = BaseEncoding.base64().decode(from)
            val in = new ByteArrayInputStream(bytes)
            val input = AvroInputStream.binary[Subscribe](in)
            input.iterator.toSeq.headOption
              .map(event => Right(event)).getOrElse(Left("Unable to bind Subscribe event"))
          case _ => Left("Unable to bind Subscribe event")
        }
      }
    }

    override def unbind(key: String, event: Subscribe): String = {
      val baos = new ByteArrayOutputStream()
      val output = AvroOutputStream.binary[Subscribe](baos)
      output.write(event)
      output.close()
      val payload = BaseEncoding.base64().encode(baos.toByteArray)
      stringBinder.unbind("data", payload)
    }
  }
}