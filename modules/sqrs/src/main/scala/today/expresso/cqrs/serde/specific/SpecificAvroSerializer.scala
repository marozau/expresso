package today.expresso.cqrs.serde.specific

import java.util
import javax.inject.Inject

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.AbstractKafkaAvroSerializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.Serializer
import today.expresso.cqrs.api.{ToKeyRecord, ToValueRecord}

/**
  * @author im.
  */
class SpecificAvroSerializer[T](implicit toKey: ToKeyRecord[T], toValue: ToValueRecord[T]) extends AbstractKafkaAvroSerializer with Serializer[T] {

  private val inner = new GenericAvroSerializer
  private var toRecord: (T) => GenericRecord = _

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {
    inner.configure(configs, isKey)
    toRecord = if (isKey) toKey.apply else toValue.apply
  }

  override def serialize(topic: String, data: T) = {
    val record = toRecord(data)
    val bytes = inner.serialize(topic, record)
    bytes
  }

  override def close() = {}

  def setSchemaRegistry(schemaRegistry: SchemaRegistryClient): Unit = {
    inner.setSchemaRegistry(schemaRegistry)
  }
}
