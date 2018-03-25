package today.expresso.cqrs.serde.specific

import java.io.{ByteArrayOutputStream, IOException}
import java.nio.ByteBuffer
import java.util

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException
import io.confluent.kafka.serializers.{AbstractKafkaAvroSerDe, AbstractKafkaAvroSerializer}
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.errors.SerializationException
import today.expresso.cqrs.api.{KeySerializer, ValueSerializer}
import today.expresso.cqrs.serde.utils.GenericAvroUtils

/**
  * @author im.
  */
class GenericAvroSerializer extends AbstractKafkaAvroSerializer with KeySerializer with ValueSerializer {

  var schemaRegistryOption: Option[SchemaRegistryClient] = None
  var postfix: String = "-value"
  var includeTopicInSubject: Boolean = false

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {
    import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
    configure(new KafkaAvroSerializerConfig(configs))
    this.postfix = if (isKey) "-key" else "-value"
    //TODO: must be false or empty
    this.includeTopicInSubject = Option(configs.get("subject.topic.include").asInstanceOf[Boolean]).getOrElse(false)
  }

  override def getSchema(data: scala.Any) = data.asInstanceOf[GenericRecord].getSchema

  /**
    * Generates subject for the schema based on full class name and topic data
    * It is possible to store different messages in the same topic without schema clashing
    *
    * @param topic The topic to produce message
    * @param data Avro generic record
    * @return The subject of the schema
    */
  def getSubject(topic: String, data: GenericRecord): String = {
      if (includeTopicInSubject)
        topic + "-" + data.getSchema.getFullName + postfix
      else
        data.getSchema.getFullName + postfix
  }

  override def serialize(topic: String, data: GenericRecord): Array[Byte] = {
    val subject = getSubject(topic, data)
    val schema = getSchema(data)
    val dataBytes = GenericAvroUtils.serialize(data)
    try {
      val id = schemaRegistryOption.getOrElse(schemaRegistry).register(subject, schema)
      val out = new ByteArrayOutputStream()
      out.write(AbstractKafkaAvroSerDe.MAGIC_BYTE)
      out.write(ByteBuffer.allocate(AbstractKafkaAvroSerDe.idSize).putInt(id).array)
      out.write(dataBytes)
      val bytes = out.toByteArray
      out.close()
      bytes
    } catch {
      case e@(_: IOException | _: RuntimeException) =>
        throw new SerializationException("Error serializing Avro message", e)
      case e: RestClientException =>
        throw new SerializationException("Error registering Avro schema: " + schema, e)
    }
  }

  override def close() = {}

  /**
    * Allows to inject test schemaRegistry
    *
    * @param schemaRegistry Schema Registry client instance
    */
  def setSchemaRegistry(schemaRegistry: SchemaRegistryClient): Unit = {
    this.schemaRegistryOption = Some(schemaRegistry)
  }
}
