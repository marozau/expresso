package today.expresso.stream.serde

import com.sksamuel.avro4s._
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.objenesis.instantiator.sun.UnsafeFactoryInstantiator
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec, WordSpecLike}
import today.expresso.stream.api._
import today.expresso.stream.domain.{Domain, Message, Serializer}
import today.expresso.stream.serde.akka.AvroSerializer
import today.expresso.stream.serde.specific.{GenericAvroSerializer, SpecificAvroDeserializer, SpecificAvroSerializer}
import today.expresso.stream.serde.utils.{GenericAvroUtils, SpecificAvroUtils}

import scala.reflect.ClassTag

/**
  * @author im.
  */
object SpecificAvroSerializerSpec {

  trait Timestampt {
    val t = System.currentTimeMillis()
  }

  @AvroDoc("test class")
  final case class TestObject(@Key userId: Long, v1: Long, @AvroDoc("optional string field") v2: Option[String]) extends Domain with Timestampt

  object TestObject extends Serializer[TestObject] {
    override def toBinary(t: TestObject) = SpecificAvroUtils.serialize[TestObject](t)
    override def fromBinary(bytes: Array[Byte]) = SpecificAvroUtils.deserialize[TestObject](bytes)
  }

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
      record.getSchema.getFullName should be("today.expresso.stream.serde.TestObject")

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
      when(mockSchemaRegistryClient.getBySubjectAndId(isNull[String], anyInt())).thenReturn(AvroSchema[TestObject])

      val specificSerializer = new SpecificAvroSerializer[TestObject]()
      val props = Map(
        AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> "localhost:8080"
      )
      import scala.collection.JavaConverters._
      specificSerializer.configure(props.asJava, isKey = false)
      specificSerializer.setSchemaRegistry(mockSchemaRegistryClient)

      val speicificDeseralizer = new SpecificAvroDeserializer[TestObject]()
      speicificDeseralizer.configure(props.asJava, isKey = false)
      speicificDeseralizer.setSchemaRegistry(mockSchemaRegistryClient)

      val testObject = TestObject(1, 2, Some("avro"))
      val bytes: Array[Byte] = specificSerializer.serialize("topic", testObject)
      val testObjectRestored: TestObject = speicificDeseralizer.deserialize("topic", bytes)


      println(testObject)
      println(testObjectRestored)
    }

    "test class[T]" in {

      val testObject = TestObject(1, 3, Some("avro1"))

      val manifest: Class[_] = classOf[TestObject]
      import scala.reflect.runtime._
      val rootMirror = universe.runtimeMirror(manifest.getClassLoader)
      var classSymbol = rootMirror.classSymbol(manifest)
      val classMirror = rootMirror.reflectClass(classSymbol)
      import scala.reflect.runtime.{currentMirror => cm}
      val companionModule: universe.ModuleSymbol = classSymbol.companion.asModule
      val instance = cm.reflectModule(companionModule).instance

      val methodSerialize = instance.getClass.getDeclaredMethod("toBinary", classOf[TestObject])
      val methodDeserialize = instance.getClass.getDeclaredMethod("fromBinary", classOf[Array[Byte]])

      val bytes = methodSerialize.invoke(instance, testObject)
      val testObjectRestored = methodDeserialize.invoke(instance, bytes)

      println("hello world of deserialization")
      println(testObject)
      println(testObjectRestored)
    }

    "test akka AvroServizer" in {

      val serde  = new AvroSerializer

      val testObject = TestObject(1, 87, Some("hello akka serialization"))
      val bytes = serde.toBinary(testObject)
      val restored = serde.fromBinary(bytes, Some(classOf[TestObject]))

      println("hello world of deserialization")
      println(testObject)
      println(restored)
    }
  }
}
