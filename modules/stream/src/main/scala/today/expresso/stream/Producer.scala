package today.expresso.stream

import java.lang.invoke.MethodHandles
import java.util.Properties
import javax.inject.Inject

import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.slf4j.LoggerFactory
import play.api.Logger
import today.expresso.stream.api.{ToKeyRecord, ToValueRecord}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
trait Producer {

  val name: String

  def send[V](topic: String, data: V)(implicit toKey: ToKeyRecord[V], toValue: ToValueRecord[V]): Future[RecordMetadata]

  def beginTransaction(): Future[Unit]

  def abortTransaction(): Future[Unit]

  def commitTransaction(): Future[Unit]
}

object Producer {
  private final val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  def transactionally[A](f: => Future[A])(implicit p: Producer, ec: ExecutionContext): Future[A] = {
    p.beginTransaction()
      .flatMap(_ => f)
      .flatMap(result => p.commitTransaction().map(_ => result))
      .recover {
        case t: Throwable =>
          logger.error(s"[Producer ${p.name}] transaction failed", t)
          p.abortTransaction()
          throw t
      }
  }
}

class ProducerImpl @Inject()(kafkaProducer: KafkaProducer[GenericRecord, GenericRecord], props: Properties)(implicit ec: ExecutionContext)
  extends Producer {

  kafkaProducer.initTransactions()

  override lazy val name = s"clientId=${props.get("client.id")}, transactionalId=${props.get("transactional.id")}"

  override def send[V](topic: String, data: V)(implicit toKey: ToKeyRecord[V], toValue: ToValueRecord[V]): Future[RecordMetadata] = Future {
    val keyRecord = toKey(data)
    val valueRecord = toValue(data)
    val record = new ProducerRecord[GenericRecord, GenericRecord](topic, null, keyRecord, valueRecord)
    kafkaProducer.send(record).get()
  }

  override def beginTransaction(): Future[Unit] = Future {
    kafkaProducer.beginTransaction()
  }

  override def abortTransaction(): Future[Unit] = Future {
    kafkaProducer.abortTransaction()
  }

  override def commitTransaction(): Future[Unit] = Future {
    kafkaProducer.commitTransaction()
  }

  def close(): Future[Unit] = Future {
    Logger.info(s"[Producer $name] close")
    kafkaProducer.close()
  }
}
