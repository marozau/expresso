package models.components

import db.Repository
import models.Recipient
import slick.jdbc.GetResult

/**
  * @author im.
  */
trait RecipientComponent {
  this: Repository =>

  import api._

  implicit val recipientStatusTypeMapper = createEnumJdbcType("recipient_status", Recipient.Status)

  implicit val recipientGetResult: GetResult[Recipient] = GetResult { r =>
    Recipient(
      uuidColumnType.getValue(r.rs, r.skip.currentPos),
      r.nextLong(),
      r.nextLong(),
      recipientStatusTypeMapper.getValue(r.rs, r.skip.currentPos),
      r.nextTimestamp().toInstant
    )
  }
}
