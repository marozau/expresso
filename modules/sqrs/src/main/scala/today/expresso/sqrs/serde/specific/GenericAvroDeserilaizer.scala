package today.expresso.sqrs.serde.specific

import java.io.IOException
import java.nio.ByteBuffer
import java.util
import javax.inject.Inject

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException
import io.confluent.kafka.serializers.AbstractKafkaAvroDeserializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Deserializer
import today.expresso.sqrs.serde.utils.GenericAvroUtils

/**
  * @author im.
  */
class GenericAvroDeserilaizer extends AbstractKafkaAvroDeserializer with Deserializer[GenericRecord] {

  var schemaRegistryOption: Option[SchemaRegistryClient] = None
  private var postfix: String = "-value"
  var includeTopicInSubject: Boolean = false

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {
    import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
    configure(new KafkaAvroDeserializerConfig(configs))
    this.postfix = if(isKey) "-key" else "-value"
    this.includeTopicInSubject = Option(configs.get("subject.topic.include").asInstanceOf[Boolean]).getOrElse(false)
  }

  /**
    * Overwrite in SpecificRecordDeserializer<Any>
    * @param topic
    * @return
    */
  def getSubject(topic: String): String = {
    topic + postfix
  }

  def getSubject(topic: String, clazz: Class[_]): String = {
    if (includeTopicInSubject)
      topic + "-" + clazz.getCanonicalName + postfix
    else
      clazz.getCanonicalName + postfix
  }

  private def getByteBuffer(payload: Array[Byte]) = {
    val buffer = ByteBuffer.wrap(payload)
    if (buffer.get != 0) throw new SerializationException("Unknown magic byte!")
    else buffer
  }

  override def deserialize(topic: String, data: Array[Byte]) = {
    val subject = getSubject(topic)
    val buffer = getByteBuffer(data)
    val id = buffer.getInt
    try {
      val schema = schemaRegistryOption.getOrElse(schemaRegistry).getBySubjectAndId(subject, id)
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

  @Inject()
  def setSchemaRegistry(schemaRegistry: SchemaRegistryClient): Unit = {
    this.schemaRegistryOption = Some(schemaRegistry)
  }
}
