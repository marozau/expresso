package today.expresso.stream

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.{Inject, Provider, Singleton}

import akka.actor.ActorSystem
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer
import play.api.inject.{ApplicationLifecycle, Module}
import play.api.{Configuration, Environment, Logger}
import today.expresso.common.utils.ConfigUtils
import today.expresso.stream.api.{KeySerializer, ValueSerializer}
import today.expresso.stream.serde.specific.GenericAvroSerializer

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class SqrsModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration) = {
    Seq(
      bind[KeySerializer].to(classOf[GenericAvroSerializer]),
      bind[ValueSerializer].to(classOf[GenericAvroSerializer])
    )
  }
}

/**
  * Producer provider creates new instance of producer with unique instance of kafka producer
  * Each producer has unique transactional id that atomically increments using counter
  *
  * @param configuration
  * @param appLifecycle
  * @param actorSystem
  * @param keySerializer
  * @param valueSerializer
  * @param counter
  * @param ec
  */
class ProducerProvider @Inject()(configuration: Configuration,
                                 keySerializer: KeySerializer,
                                 valueSerializer: ValueSerializer,
                                 counter: KafkaProducerTransactionalCounter)
                                (implicit ec: ExecutionContext, appLifecycle: ApplicationLifecycle, actorSystem: ActorSystem)
  extends Provider[Producer] {

  override def get(): Producer = {
    val config = configuration.get[Configuration]("stream.kafka.producer")
    ProducerProvider(config, keySerializer, valueSerializer, counter.get().toString)
  }
}

object ProducerProvider {

  def apply(config: Configuration,
            keySerializer: KeySerializer,
            valueSerializer: ValueSerializer,
            qualifier: String)
           (implicit ec: ExecutionContext, appLifecycle: ApplicationLifecycle, actorSystem: ActorSystem): Producer =
  {
    val props = ConfigUtils.getProperties(config)
    props.put("transactional.id", config.get[String]("transactional.id") + "-" + qualifier)
    props.put("client.id", config.get[String]("client.id") + "-" + qualifier)

    val kafkaProducer = new KafkaProducer[GenericRecord, GenericRecord](props, keySerializer, valueSerializer)
    val producer = new ProducerImpl(kafkaProducer, props)(actorSystem.dispatchers.lookup("stream.kafka.producer.blocking-dispatcher"))

    appLifecycle.addStopHook { () =>
      producer.close()
    }

    producer
  }
}

@Singleton
class KafkaProducerTransactionalCounter {

  val counter = new AtomicInteger

  def get(): Int = {
    counter.incrementAndGet()
  }
}
