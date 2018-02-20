package models.components

import db.Repository
import models.EditionWriter
import slick.jdbc.GetResult

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
