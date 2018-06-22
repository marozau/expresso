package today.expresso.stream

import java.util.Properties

import javax.inject.Inject
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
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

  def close(): Future[Unit]
}

trait ProducerTransactional extends Producer {
  def beginTransaction(): Future[Unit]

  def abortTransaction(): Future[Unit]

  def commitTransaction(): Future[Unit]
}

class ProducerImpl @Inject()(kafkaProducer: KafkaProducer[GenericRecord, GenericRecord], props: Properties)(implicit ec: ExecutionContext)
  extends Producer {

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

  override def close(): Future[Unit] = Future {
    Logger.info(s"[Producer $name] close")
    kafkaProducer.close()
  }
}

class ProducerTransactionalImpl @Inject()(kafkaProducer: KafkaProducer[GenericRecord, GenericRecord], props: Properties)(implicit ec: ExecutionContext)
  extends ProducerImpl(kafkaProducer, props) with ProducerTransactional {

  kafkaProducer.initTransactions()

  override def beginTransaction(): Future[Unit] = Future {
    kafkaProducer.beginTransaction()
  }

  override def abortTransaction(): Future[Unit] = Future {
    kafkaProducer.abortTransaction()
  }

  override def commitTransaction(): Future[Unit] = Future {
    kafkaProducer.commitTransaction()
  }
}