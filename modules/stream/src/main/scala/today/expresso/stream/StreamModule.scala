package today.expresso.stream

import java.util.UUID

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

      bind[Producer].toProvider[ProducerProvider],
      bind[ProducerTransactional].toProvider[ProducerTransactionalProvider],
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

/**
  * Producer provider creates new instance of producer with unique instance of kafka producer
  * Each producer has unique transactional id that atomically increments using counter
  *
  * @param configuration
  * @param appLifecycle
  * @param actorSystem
  * @param keySerializer
  * @param valueSerializer
  * @param ec
  */
class ProducerProvider @Inject()(configuration: Configuration,
                                 keySerializer: KeySerializer,
                                 valueSerializer: ValueSerializer)
                                (implicit ec: ExecutionContext, appLifecycle: ApplicationLifecycle, actorSystem: ActorSystem)
  extends Provider[Producer] {

  override def get(): Producer = {
    val config = configuration.get[Configuration]("stream.kafka.producer")
    ProducerProvider(config, keySerializer, valueSerializer, UUID.randomUUID().toString)
  }
}

class ProducerTransactionalProvider @Inject()(configuration: Configuration,
                                 keySerializer: KeySerializer,
                                 valueSerializer: ValueSerializer)
                                (implicit ec: ExecutionContext, appLifecycle: ApplicationLifecycle, actorSystem: ActorSystem)
  extends Provider[ProducerTransactional] {

  override def get(): ProducerTransactional = {
    val config = configuration.get[Configuration]("stream.kafka.producer")
    ProducerProvider.transactional(config, keySerializer, valueSerializer, UUID.randomUUID().toString)
  }
}

object ProducerProvider {

  def apply(config: Configuration,
            keySerializer: KeySerializer,
            valueSerializer: ValueSerializer,
            qualifier: String)
           (implicit ec: ExecutionContext, appLifecycle: ApplicationLifecycle, actorSystem: ActorSystem): Producer = {
    val props = ConfigUtils.getProperties(config)
    props.remove("transactional.id")
    props.put("client.id", config.get[String]("client.id") + "-" + qualifier)

    val kafkaProducer = new KafkaProducer[GenericRecord, GenericRecord](props, keySerializer, valueSerializer)
    val producer = new ProducerImpl(kafkaProducer, props)(actorSystem.dispatchers.lookup("stream.kafka.producer.blocking-dispatcher"))

    appLifecycle.addStopHook { () =>
      producer.close()
    }

    producer
  }

  def transactional(config: Configuration,
            keySerializer: KeySerializer,
            valueSerializer: ValueSerializer,
            qualifier: String)
           (implicit ec: ExecutionContext, appLifecycle: ApplicationLifecycle, actorSystem: ActorSystem): ProducerTransactional = {
    val props = ConfigUtils.getProperties(config)
    props.put("transactional.id", config.get[String]("transactional.id") + "-" + qualifier)
    props.put("client.id", config.get[String]("client.id") + "-" + qualifier)

    val kafkaProducer = new KafkaProducer[GenericRecord, GenericRecord](props, keySerializer, valueSerializer)
    val producer = new ProducerTransactionalImpl(kafkaProducer, props)(actorSystem.dispatchers.lookup("stream.kafka.producer.blocking-dispatcher"))

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
