package models.components

import java.time.ZonedDateTime
import java.util.UUID

import today.expresso.common.db.Repository
import models.Recipient
import today.expresso.common.utils.SqlUtils

/**
  * @author im.
  */
trait RecipientComponent {
  this: Repository =>

  import api._

  implicit val recipientStatusTypeMapper = createEnumJdbcType("recipient_status", Recipient.Status)

  case class DBRecipient(id: Option[UUID],
                         newsletterId: Long,
                         userId: Long,
                         status: Recipient.Status.Value,
                         createdTimestamp: Option[ZonedDateTime] = None,
                         modifiedTimestamp: Option[ZonedDateTime] = None)

  protected class Recipients(tag: Tag) extends Table[DBRecipient](tag, "recipients") {

    def uuid = column[UUID]("id", O.PrimaryKey, O.AutoInc)

    def newsletterId = column[Long]("newsletter_id")

    def userId = column[Long]("user_id")

    def status = column[Recipient.Status.Value]("status")

    def createdTimestamp = column[ZonedDateTime]("created_timestamp", SqlUtils.timestampTzNotNullType)

    def modifiedTimestamp = column[ZonedDateTime]("modified_timestamp", SqlUtils.timestampTzNotNullType)

    def * = (uuid.?, newsletterId, userId, status, createdTimestamp.?, modifiedTimestamp.?) <> ((DBRecipient.apply _).tupled, DBRecipient.unapply)
  }

  protected val recipients = TableQuery[Recipients]

}
