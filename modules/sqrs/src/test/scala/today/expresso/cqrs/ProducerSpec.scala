package today.expresso.cqrs

import com.sksamuel.avro4s.{AvroDoc, AvroSchema}
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import net.manub.embeddedkafka.EmbeddedKafka
import org.apache.avro.Schema
import org.mockito.ArgumentMatchers.{any, anyInt, anyString}
import org.mockito.Mockito.when
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Span}
import play.api.inject.ApplicationLifecycle
import play.api.inject.guice.GuiceApplicationBuilder
import today.expresso.cqrs.api.{Key, KeySerializer, ValueSerializer}
import today.expresso.cqrs.serde.specific.GenericAvroSerializer

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author im.
  */
object ProducerSpec {

  @AvroDoc("test class")
  case class TestObject(@Key userId: Long, v1: Long, @AvroDoc("optional string field") v2: Option[String])

  val testObject = TestObject(1, 2, Some("test2"))
}

class ProducerSpec extends WordSpec
  with WordSpecLike
  with Matchers
  with ScalaFutures
  with BeforeAndAfter
  with MockitoSugar
  with EmbeddedKafka
  with BeforeAndAfterAll {

  import ProducerSpec._

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(10000, Millis)), scaled(Span(100, Millis)))

  val mockSchemaRegistryClient = mock[SchemaRegistryClient]
  when(mockSchemaRegistryClient.register(anyString(), any(classOf[Schema]))).thenReturn(1)
  when(mockSchemaRegistryClient.getBySubjectAndId(anyString(), anyInt())).thenReturn(AvroSchema[TestObject])

  val keySerializer = {
    val serializer = new GenericAvroSerializer
    serializer.setSchemaRegistry(mockSchemaRegistryClient)
    serializer
  }

  val valueSerializer = {
    val serializer = new GenericAvroSerializer
    serializer.setSchemaRegistry(mockSchemaRegistryClient)
    serializer
  }

  import play.api.inject.bind
  val injector = new GuiceApplicationBuilder()
    .overrides(bind[KeySerializer].toInstance(keySerializer))
    .overrides(bind[ValueSerializer].toInstance(valueSerializer))
    .bindings(new SqrsModule)
    .injector()

  override protected def afterAll(): Unit = {
    super.afterAll()
    injector.instanceOf[ApplicationLifecycle].stop()
  }

  "Producer" must {

    "send message of specific class" in {

      withRunningKafka {

        implicit val producer: Producer = injector.instanceOf[Producer]

        whenReady(Producer.transactionally(producer.send("test", testObject))) { res =>
          res.topic() should be("test")
        }
      }
    }
  }
}
