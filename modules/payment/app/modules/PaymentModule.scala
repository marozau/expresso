package modules

import akka.actor.ActorSystem
import com.google.inject.{AbstractModule, Provides}
import javax.inject.Named
import org.apache.kafka.streams.StreamsConfig
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import services.PaymentSystemNames
import services.yandex.{PaymentServiceYandex, YandexService}
import streams.Names
import today.expresso.common.utils.ConfigUtils
import today.expresso.stream.{Producer, ProducerProvider}
import today.expresso.stream.api.{KeySerializer, ValueSerializer}

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
class PaymentModule extends AbstractModule {
  override def configure() = {

  }

  @Provides
  @Named(PaymentSystemNames.YANDEX)
  def providePaymentServiceYandex(yandexService: YandexService)(implicit ec: ExecutionContext) = new PaymentServiceYandex(yandexService)
}
