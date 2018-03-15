package today.expresso.sqrs.serde

import java.io.{ByteArrayOutputStream, IOException}
import java.nio.ByteBuffer
import java.util
import javax.inject.Inject

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException
import io.confluent.kafka.serializers.AbstractKafkaAvroSerializer
import org.apache.avro.Schema
import org.apache.avro.reflect.ReflectData
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Serializer
import today.expresso.sqrs.serde.utils.{ReflectAvroUtils, SpecificAvroUtils}

import scala.reflect.ClassTag

/**
  * @author im.
  */
class SpecificAvroSerializer extends AbstractKafkaAvroSerializer with Serializer[scala.Any] {

  var shemaRegistryOption: Option[SchemaRegistryClient] = None

  override def configure(configs: util.Map[String, _], isKey: Boolean) = {
    import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
    configure(new KafkaAvroSerializerConfig(configs))
  }

  override def getSchema(v: scala.Any): Schema = {
    mkSchema(v)
  }
  // scala> def mkArray[T : ClassTag](elems: T*) = Array[T](elems: _*)
  // mkArray: [T](elems: T*)(implicit evidence$1: scala.reflect.ClassTag[T])Array[T]
  private def mkSchema[T](v: T)(implicit ev: ClassTag[T]) = {
    ReflectData.get.getSchema(v.getClass)
  }

  /**
    * Allows to inject test schemaRegistry
    * @param shemaRegistry
    */
  @Inject()
  def setSchemaRegistry(shemaRegistry: SchemaRegistryClient): Unit = {
    this.shemaRegistryOption = Some(schemaRegistry)
  }

  override def serialize(topic: String, data: Any): Array[Byte] = {
    val subject = data.getClass.toString + "-value" //TODO: check Martin's commit on github
    val dataBytes = ReflectAvroUtils.serialize(data)
    val schema = getSchema(data)
    try {
      val id = shemaRegistryOption.getOrElse(schemaRegistry).register(subject, schema)
      val out = new ByteArrayOutputStream()
      out.write(0)
      out.write(ByteBuffer.allocate(4).putInt(id).array)
      out.write(dataBytes)
      val bytes = out.toByteArray
      out.close
      bytes
    } catch {
      case e@(_: IOException | _: RuntimeException) =>
        // avro serialization can throw AvroRuntimeException, NullPointerException,
        // ClassCastException, etc
        throw new SerializationException("Error serializing Avro message", e)
      case e: RestClientException =>
        throw new SerializationException("Error registering Avro schema: " + schema, e)
    }
  }

  override def close() = {}
}
