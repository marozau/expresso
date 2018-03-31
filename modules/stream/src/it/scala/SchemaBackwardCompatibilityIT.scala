import io.confluent.kafka.schemaregistry.client.{CachedSchemaRegistryClient, MockSchemaRegistryClient}
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import today.expresso.stream.api.ToValueRecord
import today.expresso.stream.domain.event.newsletter.{NewsletterEditionOpened, NewsletterEditionSent}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

/**
  * @author im.
  */
class SchemaBackwardCompatibilityIT extends WordSpecLike with Matchers with BeforeAndAfterAll {

  implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global

  val events = (
    NewsletterEditionSent(1, 1, 1, 1),
    NewsletterEditionOpened(1, 1, 1, 1)
  )

  val env = System.getenv().getOrDefault("ENV", "dev")
  val mockSchemaRegistryClient = {
    if (env == "it") new CachedSchemaRegistryClient("localhost:8081", 1000)
    else new MockSchemaRegistryClient
  }

  def checkCompatibility[V](data: V)(implicit toValue: ToValueRecord[V]): Unit = {
    val schema = toValue(data).getSchema
    mockSchemaRegistryClient.register(schema.getFullName, schema)
  }

  "Schema registry" should {

    "check that we test all possible events and commands" in {
      //      import org.reflections.Reflections
      //      val reflections = new Reflections("today.expresso.stream.event")
      //      val eventClasses = reflections.getSubTypesOf(classOf[Event])
      //      println(eventClasses)

    }

    "check events compatibility" in {
      checkCompatibility(events._1)
      checkCompatibility(events._2)
    }
  }
}
