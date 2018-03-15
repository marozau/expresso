package today.expresso.sqrs.serde

import java.io.ByteArrayOutputStream

import com.sksamuel.avro4s._
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec, WordSpecLike}
import today.expresso.sqrs.serde.SpecificAvroSerializerSpec.TestObject
import today.expresso.sqrs.serde.utils.{GenericRecordAvroUtils, ReflectAvroUtils, SpecificAvroUtils}

/**
  * @author im.
  */
object SpecificAvroSerializerSpec {

  @AvroDoc("test class")
  case class TestObject(v1: Long, @AvroDoc("optional string field") v2: Option[String])

  implicit val shemaFor = SchemaFor[TestObject]
  implicit val format = RecordFormat[TestObject]
  implicit val fromRecord = FromRecord[TestObject]

  implicit def castGeneric(t: TestObject): GenericRecord = format.to(t)
  implicit def castClass(g: GenericRecord): TestObject = format.from(g)
}

class SpecificAvroSerializerSpec extends WordSpec
  with WordSpecLike
  with Matchers
  with ScalaFutures
  with BeforeAndAfter
  with MockitoSugar {

  "SpecificAvroSerializer" must {
    "serialize objects" in {
      val serializer = new SpecificAvroSerializer()
      val mockSchemaRegistryClient = mock[SchemaRegistryClient]
      import org.mockito.ArgumentMatchers._
      import org.mockito.Mockito._
      when(mockSchemaRegistryClient.register(anyString(), any(classOf[Schema]))).thenReturn(1)
      serializer.setSchemaRegistry(mockSchemaRegistryClient)

      val testObject = TestObject(2, Some("test"))
      val schema: Schema = serializer.getSchema(testObject.asInstanceOf[Any])
      println(schema)
      schema.getFullName should be("today.expresso.sqrs.serde.SpecificAvroSerializerSpec$.TestObject")

      val bytes = ReflectAvroUtils.serialize(testObject)
//      val testObjectRestored = ReflectAvroUtils.deserialize[TestObject](bytes, schema)
    val record = GenericRecordAvroUtils.deserialize(bytes, schema)
      println(record)
      val format = RecordFormat[TestObject]
      val testObjectRestored = format.from(record)
      println(testObjectRestored)
    }
  }

  "serialize using generic record as intermediate citizen" in {
    val serializer = new SpecificAvroSerializer()
    val mockSchemaRegistryClient = mock[SchemaRegistryClient]
    import org.mockito.ArgumentMatchers._
    import org.mockito.Mockito._
    when(mockSchemaRegistryClient.register(anyString(), any(classOf[Schema]))).thenReturn(1)
    serializer.setSchemaRegistry(mockSchemaRegistryClient)

    val format = RecordFormat[TestObject]
    val schema = AvroSchema[TestObject]
    println(schema)

    val testObject = TestObject(2, Some("test"))
    val record = format.to(testObject)
    record.getSchema.getFullName should be("today.expresso.sqrs.serde.TestObject")

    val bytes = GenericRecordAvroUtils.serialize(record)
    val recordDecoded = GenericRecordAvroUtils.deserialize(bytes, record.getSchema)
    val testObjectDecoded = format.from(recordDecoded)
    println(testObjectDecoded)
  }

  "implicits" in {
    import SpecificAvroSerializerSpec._

    val testObject = TestObject(2, Some("test"))
    val record: GenericRecord = testObject
    val testObjectAgain: TestObject = record

    testObjectAgain should be (testObject)
  }

  "avro utils" in {
    implicit val shemaFor = SchemaFor[TestObject]
    implicit val format = RecordFormat[TestObject]
    implicit val fromRecord = FromRecord[TestObject]

    val testObject = TestObject(2, Some("test"))
    val testObjectRestored = SpecificAvroUtils.deserialize(SpecificAvroUtils.serialize(testObject))
    println(testObjectRestored)
  }
}
