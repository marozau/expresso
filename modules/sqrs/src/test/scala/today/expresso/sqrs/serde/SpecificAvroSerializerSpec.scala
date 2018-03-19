package today.expresso.sqrs.serde

import com.sksamuel.avro4s._
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.{AbstractKafkaAvroSerDeConfig, KafkaAvroDeserializerConfig, KafkaAvroSerializerConfig}
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec, WordSpecLike}
import today.expresso.sqrs.api._
import today.expresso.sqrs.serde.generic.SpecificAvroSerializer
import today.expresso.sqrs.serde.specific.GenericAvroSerializer
import today.expresso.sqrs.serde.utils.{GenericAvroUtils, SpecificAvroUtils}

/**
  * @author im.
  */
object SpecificAvroSerializerSpec {

  @AvroDoc("test class")
  case class TestObject(@Key userId: Long, v1: Long, @AvroDoc("optional string field") v2: Option[String])

  val testObject = TestObject(1, 2, Some("test2"))
}

class SpecificAvroSerializerSpec extends WordSpec
  with WordSpecLike
  with Matchers
  with ScalaFutures
  with BeforeAndAfter
  with MockitoSugar {

  import SpecificAvroSerializerSpec._

  "SpecificAvroSerializer" must {

    "serialize using generic record as intermediate citizen" in {
      val serializer = new GenericAvroSerializer()
      val mockSchemaRegistryClient = mock[SchemaRegistryClient]
      when(mockSchemaRegistryClient.register(anyString(), any(classOf[Schema]))).thenReturn(1)
      serializer.setSchemaRegistry(mockSchemaRegistryClient)

      val format = RecordFormat[TestObject]
      val schema = AvroSchema[TestObject]
      println(schema)

      val record = format.to(testObject)
      record.getSchema.getFullName should be("today.expresso.sqrs.serde.TestObject")

      val bytes = GenericAvroUtils.serialize(record)
      val recordDecoded = GenericAvroUtils.deserialize(bytes, record.getSchema)
      val testObjectDecoded = format.from(recordDecoded)
      println(testObjectDecoded)
    }

    "avro utils" in {
      implicit val shemaFor = SchemaFor[TestObject]
      implicit val format = RecordFormat[TestObject]
      implicit val fromRecord = FromRecord[TestObject]

      val testObjectRestored = SpecificAvroUtils.deserialize(SpecificAvroUtils.serialize(testObject))
      println(testObjectRestored)
    }

    "producer 1" in {


      def send[V](topic: String, data: V)(implicit toKey: ToKeyRecord[V], toValue: ToValueRecord[V]) = {
        val keyRecord: GenericRecord = toKey(data)
        val valueRecord: GenericRecord = toValue(data)
        (keyRecord, valueRecord)
      }


      val (key, value) = send("test", testObject)

      println(key)
      println(value.getSchema)

      println(AvroSchema[(Long, Long)])
    }

    "producer transactional" in {


    }

    "deserialization" in {
      def send[V](topic: String, data: V)(implicit toKey: ToKeyRecord[V], toValue: ToValueRecord[V]) = {
        val keyRecord: GenericRecord = toKey(data)
        val valueRecord: GenericRecord = toValue(data)
        (keyRecord, valueRecord)
      }

      val (key, value) = send("test", testObject)
      println(key.getSchema)

      println(value.getSchema.getFullName)
      println(classOf[TestObject].getName)

      val keyBytes = GenericAvroUtils.serialize(key)
      val valueBytes = GenericAvroUtils.serialize(value)

      val keyDecoded = GenericAvroUtils.deserialize(keyBytes, key.getSchema)
      val valueDecoded = GenericAvroUtils.deserialize(valueBytes, value.getSchema)

      // implicit conversion using ToCaseClass macros
      def receive[K, V](key: GenericRecord, value: GenericRecord)(implicit toKey: ToCaseClass[K], toValue: ToCaseClass[V]) = {
        val keyRecord: K = toKey(key)
        val valueRecord: V = toValue(value)
        (keyRecord, valueRecord)
      }
      val (keyRestored, valueRestored) = receive[KeyRecord[Long], TestObject](keyDecoded, valueDecoded)

      println(keyRestored)
      println(valueRestored)
    }

    "specific avro serde" in {
      implicit val schemaFor = SchemaFor[TestObject]
      val mockSchemaRegistryClient = mock[SchemaRegistryClient]
      when(mockSchemaRegistryClient.register(anyString(), any(classOf[Schema]))).thenReturn(1)
      when(mockSchemaRegistryClient.getBySubjectAndId(anyString(), anyInt())).thenReturn(AvroSchema[TestObject])

      val specificSerializer = new today.expresso.sqrs.serde.specific.SpecificAvroSerializer[TestObject]()
      val props = Map(
        AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> "localhost:8080"
      )
      import scala.collection.JavaConverters._
      specificSerializer.configure(props.asJava, isKey = false)
      specificSerializer.setSchemaRegistry(mockSchemaRegistryClient)

      val speicificDeseralizer = new today.expresso.sqrs.serde.specific.SpecificAvroDeserializer[TestObject]()
      speicificDeseralizer.configure(props.asJava, isKey = false)
      speicificDeseralizer.setSchemaRegistry(mockSchemaRegistryClient)

      val testObject = TestObject(1, 2, Some("avro"))
      val bytes: Array[Byte] = specificSerializer.serialize("topic", testObject)
      val testObjectRestored: TestObject = speicificDeseralizer.deserialize("topic", bytes)


      println(testObject)
      println(testObjectRestored)
    }
  }
}
