package models.components

import com.github.tminglei.slickpg.utils.PlainSQLUtils
import today.expresso.common.db.Repository
import models.Recipient
import slick.jdbc.{GetResult, JdbcType, SetParameter}

/**
  * @author im.
  */
trait RecipientComponent {
  this: Repository =>

  import api._

  implicit val recipientStatusTypeMapper: JdbcType[Recipient.Status.Value] = createEnumJdbcType("RECIPIENT_STATUS", Recipient.Status)
  implicit val recipientStatusListTypeMapper: JdbcType[List[Recipient.Status.Value]] = createEnumListJdbcType("RECIPIENT_STATUS", Recipient.Status)
  implicit val recipientStatusMethodBuilder: api.Rep[Recipient.Status.Value] => EnumColumnExtensionMethods[Recipient.Status.Value, Recipient.Status.Value] = createEnumColumnExtensionMethodsBuilder(Recipient.Status)
  implicit val recipientStatusOptionMethodBuilder: api.Rep[Option[Recipient.Status.Value]] => EnumColumnExtensionMethods[Recipient.Status.Value, Option[Recipient.Status.Value]] = createEnumOptionColumnExtensionMethodsBuilder(Recipient.Status)

  implicit val recipientStatusSetParameter: SetParameter[Recipient.Status.Value] = PlainSQLUtils.mkSetParameter[Recipient.Status.Value]("RECIPIENT_STATUS")
  implicit val recipientStatusOptionSetParameter: SetParameter[Option[Recipient.Status.Value]] = PlainSQLUtils.mkOptionSetParameter[Recipient.Status.Value]("RECIPIENT_STATUS")

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
