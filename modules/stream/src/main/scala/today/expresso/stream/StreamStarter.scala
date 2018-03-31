package today.expresso.stream

import javax.inject.{Inject, Singleton}
import org.apache.kafka.streams.KafkaStreams
import play.api.Logger
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
case class StreamStarter @Inject()(kafkaStreams: KafkaStreams, appLifecycle: ApplicationLifecycle)(implicit ec: ExecutionContext) {

  kafkaStreams.start()

  appLifecycle.addStopHook(() =>
    Future {
      kafkaStreams.close()
    }
  )
}
