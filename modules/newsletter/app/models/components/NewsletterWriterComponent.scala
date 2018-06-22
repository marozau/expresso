package models.components

import today.expresso.common.db.Repository
import slick.jdbc.GetResult
import today.expresso.stream.domain.model.newsletter.NewsletterWriter

/**
  * @author im.
  */
trait NewsletterWriterComponent {
  this: Repository =>

  import api._

  implicit val newsletterWriterGetResult: GetResult[NewsletterWriter] = GetResult { r =>
    NewsletterWriter(
      uuidColumnType.getValue(r.rs, r.skip.currentPos),
      r.nextLong(),
      r.nextLong,
      r.nextTimestamp().toInstant
    )
  }
}
