package models.components

import db.Repository
import models.NewsletterWriter
import slick.jdbc.GetResult

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
