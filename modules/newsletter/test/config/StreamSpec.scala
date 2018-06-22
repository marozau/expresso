package config

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import org.apache.kafka.clients.producer.RecordMetadata
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import today.expresso.stream.{Producer, ProducerTransactional}
import today.expresso.stream.api.{ToKeyRecord, ToValueRecord}
import today.expresso.stream.domain.event.newsletter.{CampaignStarted, CampaignUpdated, NewsletterCreated, NewsletterUpdated}

import scala.concurrent.Future

/**
  * @author im.
  */
trait StreamSpec extends MockitoSugar {

  val mockSchemaRegistryClient = new MockSchemaRegistryClient

  val mockProducer = mock[Producer]
  when(mockProducer.send(any(classOf[NewsletterCreated]))(any(classOf[ToKeyRecord[NewsletterCreated]]), any(classOf[ToValueRecord[NewsletterCreated]])))
    .thenReturn(Future.successful(null.asInstanceOf[RecordMetadata]))
  when(mockProducer.send(any(classOf[NewsletterUpdated]))(any(classOf[ToKeyRecord[NewsletterUpdated]]), any(classOf[ToValueRecord[NewsletterUpdated]])))
    .thenReturn(Future.successful(null.asInstanceOf[RecordMetadata]))

  when(mockProducer.send(any(classOf[CampaignStarted]))(any(classOf[ToKeyRecord[CampaignStarted]]), any(classOf[ToValueRecord[CampaignStarted]])))
    .thenReturn(Future.successful(null.asInstanceOf[RecordMetadata]))
  when(mockProducer.send(any(classOf[CampaignUpdated]))(any(classOf[ToKeyRecord[CampaignUpdated]]), any(classOf[ToValueRecord[CampaignUpdated]])))
    .thenReturn(Future.successful(null.asInstanceOf[RecordMetadata]))

  val mockProducerTransactional = mock[ProducerTransactional]
  when(mockProducerTransactional.beginTransaction()).thenReturn(Future.unit)
  when(mockProducerTransactional.commitTransaction()).thenReturn(Future.unit)
  when(mockProducerTransactional.abortTransaction()).thenReturn(Future.unit)
}
