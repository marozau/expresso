package streams

import javax.inject.{Inject, Singleton}
import org.apache.kafka.streams.StreamsBuilder

/**
  * @author im.
  */
@Singleton
class UsersStream @Inject() (streamsBuilder: StreamsBuilder) {

}
