package streams

import javax.inject.{Inject, Singleton}
import org.apache.kafka.streams.StreamsBuilder
import play.api.Logger
import services.NewsletterService
import today.expresso.stream.domain.event.newsletter.NewsletterCreated
import today.expresso.stream.utils.TopicUtils

/**
  * @author im.
  */
@Singleton
class NewsletterStream @Inject() (builder: StreamsBuilder, newsletterService: NewsletterService) {

  Logger.info("starting NewsletterStream")

  val newsletterCreatedStream = builder.stream(TopicUtils.singleObjectTopic[NewsletterCreated])
}
