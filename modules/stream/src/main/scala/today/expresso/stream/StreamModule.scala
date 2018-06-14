package today.expresso.stream

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import javax.inject.{Inject, Provider, Singleton}
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig}
import play.api.inject.{ApplicationLifecycle, Module}
import play.api.{Configuration, Environment}
import today.expresso.common.utils.ConfigUtils
import today.expresso.stream.api.{KeySerializer, ValueSerializer}
import today.expresso.stream.serde.specific.GenericAvroSerializer

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class StreamModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration) = {
    Seq(
      bind[KeySerializer].toProvider[KeySerializerProvider],
      bind[ValueSerializer].toProvider[ValueSerializerProvider],

      bind[ProducerPool].toProvider[ProducerPoolProvider],

      bind[KafkaStreams].toProvider[KafkaStreamsProvider]
    )
  }
}

class KeySerializerProvider @Inject()(configuration: Configuration) extends Provider[KeySerializer] {
  override def get() = {
    val serializer = new GenericAvroSerializer
    val config = configuration.get[Configuration]("stream.kafka.producer")
    val configMap = ConfigUtils.getJavaConfigMap(config)
    serializer.configure(configMap, isKey = true)
    serializer
  }
}

class ValueSerializerProvider @Inject()(configuration: Configuration) extends Provider[ValueSerializer] {
  override def get() = {
    val serializer = new GenericAvroSerializer
    val config = configuration.get[Configuration]("stream.kafka.producer")
    val configMap = ConfigUtils.getJavaConfigMap(config)
    serializer.configure(configMap, isKey = false)
    serializer
  }
}

@Singleton
class KafkaProducerTransactionalCounter {

  val counter = new AtomicInteger

  def get(): Int = {
    counter.incrementAndGet()
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

class ProducerPoolProvider @Inject()(configuration: Configuration,
                                     keySerializer: KeySerializer,
                                     valueSerializer: ValueSerializer)
                                    (implicit ec: ExecutionContext, appLifecycle: ApplicationLifecycle, actorSystem: ActorSystem)
  extends Provider[ProducerPool] {

  override def get(): ProducerPool = {
    val config = configuration.get[Configuration]("stream.kafka.producer")
    val size = config.get[Int]("pool.size")
    val queue = new ArrayBlockingQueue[Producer](size)
    (1 to size).map(i => queue.add(ProducerProvider(config, keySerializer, valueSerializer, i.toString)))
    new ProducerPoolArrayBlockingQueueImpl(queue)(actorSystem.dispatchers.lookup("stream.kafka.producer.blocking-dispatcher"))
  }
}

object ProducerProvider {

  def apply(config: Configuration,
            keySerializer: KeySerializer,
            valueSerializer: ValueSerializer,
            qualifier: String)
           (implicit ec: ExecutionContext, appLifecycle: ApplicationLifecycle, actorSystem: ActorSystem): Producer = {
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

class KafkaStreamConfigProvider @Inject()(configuration: Configuration) extends Provider[StreamsConfig] {
  def get(): StreamsConfig = {
    val config = configuration.get[Configuration]("stream.kafka.streams")
    val props = ConfigUtils.getProperties(config)
    //    props.put("application.id", config.get[String]("application.id")) //TODO: add qualifier if needed
    new StreamsConfig(props)
  }
}

class KafkaStreamsProvider @Inject()(streamsBuilder: StreamsBuilder, streamsConfig: StreamsConfig)
                                    (implicit ec: ExecutionContext, appLifecycle: ApplicationLifecycle) extends Provider[KafkaStreams] {
  def get(): KafkaStreams = {
    new KafkaStreams(streamsBuilder.build, streamsConfig)
  }
}
