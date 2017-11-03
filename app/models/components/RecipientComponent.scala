package models.components

import java.time.ZonedDateTime

import models.RecipientStatus
import models.api.Repository
import utils.SqlUtils

/**
  * @author im.
  */
trait RecipientComponent {
  this: Repository with UserComponent with RecipientListComponent =>

  import api._

  implicit val recipientStatusTypeMapper = createEnumJdbcType("recipient_status", RecipientStatus)

  case class DBRecipient(listId: Long,
                         userId: Long,
                         status: RecipientStatus.Value,
                         createdTimestamp: Option[ZonedDateTime] = None,
                         modifiedTimestamp: Option[ZonedDateTime] = None)

  protected class Recipients(tag: Tag) extends Table[DBRecipient](tag, "recipients") {

    def listId = column[Long]("list_id")

    def userId = column[Long]("user_id")

    def status = column[RecipientStatus.Value]("status")

    def createdTimestamp = column[ZonedDateTime]("created_timestamp", SqlUtils.timestampTzNotNullType)

    def modifiedTimestamp = column[ZonedDateTime]("modified_timestamp", SqlUtils.timestampTzNotNullType)

    def * = (listId, userId, status, createdTimestamp.?, modifiedTimestamp.?) <> ((DBRecipient.apply _).tupled, DBRecipient.unapply)

    def listIdSupplier = foreignKey("recipients_list_id_fkey", userId, rlists)(_.id)
    def userIdSupplier = foreignKey("recipients_user_id_fkey", userId, users)(_.id)
  }

  protected val recipients = TableQuery[Recipients]

}
