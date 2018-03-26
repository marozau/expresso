package today.expresso.cqrs.serde.specific

import java.util
import javax.inject.Inject

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.AbstractKafkaAvroDeserializer
import org.apache.kafka.common.serialization.Deserializer
import today.expresso.cqrs.api.ToCaseClass

/**
  * @author im.
  */
class SpecificAvroDeserializer[T](implicit toCaseClass: ToCaseClass[T]) extends AbstractKafkaAvroDeserializer with Deserializer[T] {

  val inner = new GenericAvroDeserializer

  override def close() = {
  }

  override def deserialize(topic: String, data: Array[Byte]) = {
    val record = inner.deserialize(topic, data)
    toCaseClass(record)
  }

  override def configure(configs: util.Map[String, _], isKey: Boolean) = {
    inner.configure(configs, isKey)
  }

  def setSchemaRegistry(schemaRegistry: SchemaRegistryClient): Unit = {
    inner.setSchemaRegistry(schemaRegistry)
  }
}
