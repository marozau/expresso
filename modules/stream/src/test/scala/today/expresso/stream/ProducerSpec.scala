package today.expresso.stream

import com.sksamuel.avro4s.AvroDoc
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.kafka.streams.StreamsConfig
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Span}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{ApplicationLifecycle, bind}
import today.expresso.stream.api.{Key, KeyRecord, KeySerializer, ValueSerializer}
import today.expresso.stream.serde.specific.{GenericAvroSerializer, SpecificAvroDeserializer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.TimeoutException
import scala.concurrent.duration._

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
  with BeforeAndAfterAll
  with BeforeAndAfterEach {

  import ProducerSpec._

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(10000, Millis)), scaled(Span(100, Millis)))

  implicit val config: EmbeddedKafkaConfig = EmbeddedKafkaConfig(customConsumerProperties = Map("isolation.level" -> "read_committed"))

  val mockSchemaRegistryClient = new MockSchemaRegistryClient

  val keySerializer = {
    val serializer = new GenericAvroSerializer
    val props = Map(
      AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> "fake"
    )
    import scala.collection.JavaConverters._
    serializer.configure(props.asJava, isKey = true)
    serializer.setSchemaRegistry(mockSchemaRegistryClient)
    serializer
  }

  val valueSerializer = {
    val serializer = new GenericAvroSerializer
    val props = Map(
      AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> "fake"
    )
    import scala.collection.JavaConverters._
    serializer.configure(props.asJava, isKey = false)
    serializer.setSchemaRegistry(mockSchemaRegistryClient)
    serializer
  }

  implicit val keyDeserializer: SpecificAvroDeserializer[KeyRecord[Long]] = {
    val deserializer = new SpecificAvroDeserializer[KeyRecord[Long]]()
    val props = Map(
      AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> "fake"
    )
    import scala.collection.JavaConverters._
    deserializer.configure(props.asJava, isKey = true)

    deserializer.setSchemaRegistry(mockSchemaRegistryClient)
    deserializer
  }

  implicit val valueDeserializer: SpecificAvroDeserializer[TestObject] = {
    val deserializer = new SpecificAvroDeserializer[TestObject]()
    val props = Map(
      AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> "fake"
    )
    import scala.collection.JavaConverters._
    deserializer.configure(props.asJava, isKey = false)
    deserializer.setSchemaRegistry(mockSchemaRegistryClient)
    deserializer
  }

  val injector = new GuiceApplicationBuilder()
    .overrides(bind[KeySerializer].toInstance(keySerializer))
    .overrides(bind[ValueSerializer].toInstance(valueSerializer))
    .bindings(new StreamModule)
    .bindings(bind[StreamsConfig].toProvider[KafkaStreamConfigProvider])
    .injector()

  override protected def afterAll(): Unit = {
    super.afterAll()
  }


  override protected def beforeEach(): Unit = {
    super.beforeEach()
    EmbeddedKafka.start()
    while (!EmbeddedKafka.isRunning) {
      Thread.sleep(500)
    }
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    EmbeddedKafka.stop()
    while (EmbeddedKafka.isRunning) {
      Thread.sleep(500)
    }
    injector.instanceOf[ApplicationLifecycle].stop()
  }


  "Producer" must {

    "send message in transaction and consume" in {

      implicit val pp: ProducerPool = injector.instanceOf[ProducerPool]

      whenReady(pp.transaction(producer => producer.send("test", testObject))) { res =>
        res.topic() should be("test")
      }

      val (key, value) = consumeFirstKeyedMessageFrom[KeyRecord[Long], TestObject]("test", autoCommit = true)
      key should be(KeyRecord[Long](1))
      value should be(testObject)
    }

    "send message in transaction and fail" in {

      implicit val pp: ProducerPool = injector.instanceOf[ProducerPool]

      whenReady(
        pp.transaction(producer => producer.send("test", testObject)
          .map { result => throw new RuntimeException; result }).failed //throws fake exception to emulate failure
      ) { error =>
        error.isInstanceOf[RuntimeException] should be(true)
      }

      // no messages in the topic and it should fail with timeout
      intercept[TimeoutException] {
        consumeNumberKeyedMessagesFromTopics[KeyRecord[Long], TestObject](Set("test"), 1, true, 5000.milli)
      }
    }
  }
}
