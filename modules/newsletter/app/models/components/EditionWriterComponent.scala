package models.components

import today.expresso.common.db.Repository
import slick.jdbc.GetResult
import today.expresso.stream.domain.model.newsletter.EditionWriter

/**
  * @author im.
  */
trait EditionWriterComponent {
  this: Repository =>

  import api._

  implicit val editionWriterGetResult: GetResult[EditionWriter] = GetResult { r =>
    EditionWriter(
      uuidColumnType.getValue(r.rs, r.skip.currentPos),
      r.nextLong(),
      r.nextLong,
      r.nextTimestamp().toInstant
    )
  }
}
