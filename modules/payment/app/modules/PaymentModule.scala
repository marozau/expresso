package modules

import com.google.inject.{AbstractModule, Provides}
import javax.inject.Named
import services.{PaymentServiceYandex, PaymentSystemNames}

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
class PaymentModule extends AbstractModule {
  override def configure() = {

  }

  @Provides
  @Named(PaymentSystemNames.YANDEX)
  def providePaymentServiceYandex()(implicit ec: ExecutionContext) = new PaymentServiceYandex
}
