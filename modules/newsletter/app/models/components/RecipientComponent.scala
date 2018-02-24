package models.components

import com.github.tminglei.slickpg.utils.PlainSQLUtils
import db.Repository
import models.Recipient
import slick.jdbc.{GetResult, SetParameter}

/**
  * @author im.
  */
trait RecipientComponent {
  this: Repository =>

  import api._

  implicit val recipientStatusTypeMapper = createEnumJdbcType("recipient_status", Recipient.Status)
  implicit val recipientStatusListTypeMapper = createEnumListJdbcType("recipient_status", Recipient.Status)
  implicit val recipientStatusMethodBuilder = createEnumColumnExtensionMethodsBuilder(Recipient.Status)
  implicit val recipientStatusOptionMethodBuilder = createEnumOptionColumnExtensionMethodsBuilder(Recipient.Status)

  implicit val recipientStatusSetParameter = PlainSQLUtils.mkSetParameter[Recipient.Status.Value]("recipient_status")
  implicit val recipientStatusOptionSetParameter = PlainSQLUtils.mkSetParameter[Option[Recipient.Status.Value]]("recipient_status")

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
