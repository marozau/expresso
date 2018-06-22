package today.expresso.stream.domain.model.newsletter

import java.time.Instant
import java.util.UUID

/**
  * @author im.
  */
case class NewsletterWriter(id: UUID,
                            newsletterId: Long,
                            userId: Long,
                            createdTimestamp: Instant)
