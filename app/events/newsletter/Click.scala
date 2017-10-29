package events.newsletter

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.google.common.io.BaseEncoding
import com.sksamuel.avro4s._
import events.Tracking
import play.api.mvc.QueryStringBindable

/**
  * @author im.
  */
// TODO: to generate sequencial ID from database we need cache range.
// TODO: to calculate optimal range size it is better to analyse mailchimp data
case class Click(
                  @AvroDoc("urls table id") urlId: Long,
                  @AvroDoc("users table id") userId: Long,
                  @AvroDoc("campaigns table id") campaignId: Long,
                  @AvroDoc("server side event id") eventId: Option[Long],
                  @AvroDoc("server side event timestamp") timestamp: Option[Long] = None,
                  @AvroDoc("client's ip address, can be a proxy server address") ip: Option[String] = None,
                  useragent: Option[String] = None) extends Tracking


object Click {
  implicit val schemaFor = SchemaFor[Click]
  val format = RecordFormat[Click]

  // TODO: replace by confluent serializer with schema registry
  // TODO: fallback strategy in case when schema registry or other 3rd party is not available
  // TODO: fallback strategy, read first bytes manually
  implicit def queryStringBindable(implicit stringBinder: QueryStringBindable[String]) = new QueryStringBindable[Click] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Click]] = {
      for {
        data <- stringBinder.bind("data", params)
      } yield {
        data match {
          case (Right(from)) =>
            val bytes = BaseEncoding.base64().decode(from)
            val in = new ByteArrayInputStream(bytes)
            val input = AvroInputStream.binary[Click](in)
            input.iterator.toSeq.headOption
              .map(click => Right(click)).getOrElse(Left("Unable to bind a Click"))
          case _ => Left("Unable to bind a Click")
        }
      }
    }

    override def unbind(key: String, click: Click): String = {
      val baos = new ByteArrayOutputStream()
      val output = AvroOutputStream.binary[Click](baos)
      output.write(click)
      output.close()
      val payload = BaseEncoding.base64().encode(baos.toByteArray)
      stringBinder.unbind("data", payload)
    }
  }
}