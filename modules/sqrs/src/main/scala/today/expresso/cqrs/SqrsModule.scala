package today.expresso.cqrs

import javax.inject.{Inject, Provider, Singleton}

import akka.actor.ActorSystem
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer
import play.api.inject.{ApplicationLifecycle, Module}
import play.api.{Configuration, Environment, Logger}
import today.expresso.common.utils.ConfigUtils
import today.expresso.cqrs.api.{KeySerializer, ValueSerializer}
import today.expresso.cqrs.serde.specific.GenericAvroSerializer

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class SqrsModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration) = {
    Seq(
      bind[KeySerializer].to(classOf[GenericAvroSerializer]),
      bind[ValueSerializer].to(classOf[GenericAvroSerializer]),
      bind[KafkaProducer[GenericRecord, GenericRecord]].toProvider[KafkaProducerProvider],
      bind[Producer].toProvider[ProducerProvider]
    )
  }
}

class ProducerProvider @Inject()(configuration: Configuration,
                                 actorSystem: ActorSystem,
                                 KafkaProducerProvider: KafkaProducerProvider)
  extends Provider[Producer] {

  override def get(): Producer = {
    new ProducerImpl(KafkaProducerProvider.get())(actorSystem.dispatchers.lookup("stream.kafka.producer.blocking-dispatcher"))
  }
}

class KafkaProducerProvider @Inject()(configuration: Configuration,
                                      appLifecycle: ApplicationLifecycle,
                                      actorSystem: ActorSystem,
                                      keySerializer: KeySerializer,
                                      valueSerializer: ValueSerializer)(implicit ec: ExecutionContext)
  extends Provider[KafkaProducer[GenericRecord, GenericRecord]] {

  override def get(): KafkaProducer[GenericRecord, GenericRecord] = {
    val props = ConfigUtils.getProperties(configuration.get[Configuration]("stream.kafka.producer"))

    val kafkaProducer = new KafkaProducer[GenericRecord, GenericRecord](props, keySerializer, valueSerializer)
    kafkaProducer.initTransactions()

    appLifecycle.addStopHook { () =>
      Future {
        Logger.info(s"[Producer clientId=${props.get("client.id")}, transactionalId=${props.get("transactional.id")}] shutdown")
        kafkaProducer.close()
      }
    }

    kafkaProducer
  }
}
