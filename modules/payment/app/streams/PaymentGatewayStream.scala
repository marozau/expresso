package streams

import javax.inject.{Inject, Singleton}
import org.apache.kafka.streams.StreamsBuilder
import play.api.Logger

/**
  * @author im.
  */
@Singleton
class PaymentGatewayStream @Inject()(builder: StreamsBuilder) {

  Logger.info("starting PaymentGatewayStream")
}
