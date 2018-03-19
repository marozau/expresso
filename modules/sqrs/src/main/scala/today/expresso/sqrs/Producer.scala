package today.expresso.sqrs

import java.lang.invoke.MethodHandles
import javax.inject.Inject

import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.slf4j.LoggerFactory
import today.expresso.sqrs.api.{ToKeyRecord, ToValueRecord}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
trait Producer {
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
          logger.error("producer transaction failed", t)
          p.abortTransaction()
          throw t
      }
  }
}

class ProducerImpl @Inject()(producer: KafkaProducer[GenericRecord, GenericRecord])(implicit ec: ExecutionContext)
  extends Producer {

  override def send[V](topic: String, data: V)(implicit toKey: ToKeyRecord[V], toValue: ToValueRecord[V]): Future[RecordMetadata] = Future {
    val keyRecord = toKey(data)
    val valueRecord = toValue(data)
    val record = new ProducerRecord[GenericRecord, GenericRecord](topic, null, keyRecord, valueRecord)
    //TODO: blocking dispatcher
    producer.send(record).get()
  }

  override def beginTransaction(): Future[Unit] = Future {
    producer.beginTransaction()
  }

  override def abortTransaction(): Future[Unit] = Future {
    producer.abortTransaction()
  }

  override def commitTransaction(): Future[Unit] = Future {
    producer.commitTransaction()
  }
}
