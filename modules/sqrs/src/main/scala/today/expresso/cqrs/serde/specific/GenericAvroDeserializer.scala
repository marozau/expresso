package today.expresso.cqrs.serde.specific

import java.io.IOException
import java.nio.ByteBuffer
import java.util
import javax.inject.Inject

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException
import io.confluent.kafka.serializers.{AbstractKafkaAvroDeserializer, AbstractKafkaAvroSerDe}
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Deserializer
import today.expresso.cqrs.serde.utils.GenericAvroUtils

/**
  * @author im.
  */
class GenericAvroDeserializer extends AbstractKafkaAvroDeserializer with Deserializer[GenericRecord] {

  var schemaRegistryOption: Option[SchemaRegistryClient] = None
  var includeTopicInSubject: Boolean = false

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {
    import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
    configure(new KafkaAvroDeserializerConfig(configs))
    this.includeTopicInSubject = Option(configs.get("subject.topic.include").asInstanceOf[Boolean]).getOrElse(false)
  }

  private def getByteBuffer(payload: Array[Byte]) = {
    val buffer = ByteBuffer.wrap(payload)
    if (buffer.get != AbstractKafkaAvroSerDe.MAGIC_BYTE) throw new SerializationException("Unknown magic byte!")
    else buffer
  }

  override def deserialize(topic: String, data: Array[Byte]): GenericRecord = {
    if (data == null) return null

    var id = -1
    try {
      val buffer = getByteBuffer(data)
      id = buffer.getInt
      val schema = schemaRegistryOption.getOrElse(schemaRegistry).getBySubjectAndId(null, id)
      val start = buffer.position + buffer.arrayOffset
      val length = buffer.limit - 1 - 4
      val record = GenericAvroUtils.deserialize(buffer.array(), start, length, schema)
      record
    } catch {
      case e@(_: IOException | _: RuntimeException) =>
        throw new SerializationException("Error deserializing Avro message", e)
      case e: RestClientException =>
        throw new SerializationException("Error retrieving Avro schema for id: " + id, e)
    }
  }

  override def close() = {

  }

  def setSchemaRegistry(schemaRegistry: SchemaRegistryClient): Unit = {
    this.schemaRegistryOption = Some(schemaRegistry)
  }
}
