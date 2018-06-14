package today.expresso.stream

import java.lang.invoke.MethodHandles
import java.util.Properties
import java.util.concurrent.{ArrayBlockingQueue, TimeUnit}

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

  def send[V](data: V)(implicit toKey: ToKeyRecord[V], toValue: ToValueRecord[V]): Future[RecordMetadata]

  def send[V](topic: String, data: V)(implicit toKey: ToKeyRecord[V], toValue: ToValueRecord[V]): Future[RecordMetadata]

  def beginTransaction(): Future[Unit]

  def abortTransaction(): Future[Unit]

  def commitTransaction(): Future[Unit]
}

class ProducerImpl @Inject()(kafkaProducer: KafkaProducer[GenericRecord, GenericRecord], props: Properties)(implicit ec: ExecutionContext)
  extends Producer {

  kafkaProducer.initTransactions()

  override lazy val name = s"clientId=${props.get("client.id")}, transactionalId=${props.get("transactional.id")}"

  override def send[V](data: V)(implicit toKey: ToKeyRecord[V], toValue: ToValueRecord[V]): Future[RecordMetadata] = Future {
    val keyRecord = toKey(data)
    val valueRecord = toValue(data)
    val topic = valueRecord.getSchema.getFullName
    val record = new ProducerRecord[GenericRecord, GenericRecord](topic, null, keyRecord, valueRecord)
    kafkaProducer.send(record).get()
  }

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

trait ProducerPool {
  def transaction[A](f: Producer => Future[A]): Future[A]

  def nontransaction[A](f: Producer => Future[A]): Future[A]
}

class ProducerPoolArrayBlockingQueueImpl(queue: ArrayBlockingQueue[Producer])(implicit ec: ExecutionContext) extends ProducerPool {

  private final val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  override def transaction[A](f: Producer => Future[A]): Future[A] = {
    Future(queue.poll(5000, TimeUnit.MILLISECONDS))
      .flatMap { producer =>
        producer.beginTransaction()
          .flatMap(_ => f(producer))
          .flatMap(result =>
            producer.commitTransaction().map { _ =>
              queue.add(producer)
              result
            }
          )
          .recover {
            case t: Throwable =>
              logger.warn(s"[Producer ${producer.name}] transaction failed, message=${t.getMessage}", t)
              producer.abortTransaction()
              queue.add(producer)
              throw t
          }
      }
  }

  override def nontransaction[A](f: Producer => Future[A]): Future[A] = {
    //TODO: move timeout to config
    Future(queue.poll(5000, TimeUnit.MILLISECONDS))
      .flatMap { producer =>
        f(producer).map { result => queue.add(producer); result }
          .recover {
            case t: Throwable =>
              logger.warn(s"[Producer ${producer.name}] failed, message=${t.getMessage}", t)
              queue.add(producer)
              throw t
          }
      }
  }
}
