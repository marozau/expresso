package modules

import java.util.TimeZone

import akka.actor.ActorSystem
import clients.{GuiceJobFactory, Quartz, QuartzFutureImpl}
import com.google.inject.{AbstractModule, Provides}
import javax.inject.Named
import org.apache.kafka.streams.{StreamsBuilder, StreamsConfig}
import org.quartz.spi.JobFactory
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.api.libs.concurrent.AkkaGuiceSupport
import services.{MailService, MailServiceImpl, UserService, UserServiceImpl}
import streams.{Names, NewsletterStream}
import today.expresso.common.utils.ConfigUtils
import today.expresso.stream.api.{KeySerializer, ValueSerializer}
import today.expresso.stream.{Producer, ProducerProvider}

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
class NewsletterModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    bind(classOf[JobFactory]).to(classOf[GuiceJobFactory])
    bind(classOf[Quartz]).to(classOf[QuartzFutureImpl])

    bind(classOf[UserService]).to(classOf[UserServiceImpl])
    bind(classOf[MailService]).to(classOf[MailServiceImpl])

    // streams
    bind(classOf[StreamsBuilder]).asEagerSingleton()
    bind(classOf[NewsletterStream]).asEagerSingleton()
  }

  @Provides
  @Named(Names.campaign)
  def provideCampaignProducer(config: Configuration,
                                keySerializer: KeySerializer,
                                valueSerializer: ValueSerializer)
                               (implicit ec: ExecutionContext, appLifecycle: ApplicationLifecycle, actorSystem: ActorSystem): Producer = {
    ProducerProvider(config.get[Configuration]("stream.kafka.producer"), keySerializer, valueSerializer, Names.campaign)
  }

  @Provides
  @Named(Names.newsletter)
  def provideNewsletterProducer(config: Configuration,
                                keySerializer: KeySerializer,
                                valueSerializer: ValueSerializer)
                               (implicit ec: ExecutionContext, appLifecycle: ApplicationLifecycle, actorSystem: ActorSystem): Producer = {
    ProducerProvider(config.get[Configuration]("stream.kafka.producer"), keySerializer, valueSerializer, Names.newsletter)
  }

  @Provides
  @Named(Names.edition)
  def provideEditionProducer(configuration: Configuration,
                             keySerializer: KeySerializer,
                             valueSerializer: ValueSerializer)
                            (implicit ec: ExecutionContext, appLifecycle: ApplicationLifecycle, actorSystem: ActorSystem): Producer = {
    ProducerProvider(configuration.get[Configuration]("stream.kafka.producer"), keySerializer, valueSerializer, Names.edition)
  }

  @Provides
  def provideStreamsConfig(configuration: Configuration): StreamsConfig = {
    val config = configuration.get[Configuration]("stream.kafka.streams")
    val props = ConfigUtils.getProperties(config)
//    props.put("application.id", config.get[String]("application.id")) //TODO: add qualifier if needed
    new StreamsConfig(props)
  }
}
