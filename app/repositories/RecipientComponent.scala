package repositories

import java.time.ZonedDateTime

import models.Recipient
import utils.SqlUtils

/**
  * @author im.
  */
trait RecipientComponent {
  this: Repository =>

  import api._

  protected class Recipients(tag: Tag) extends Table[Recipient](tag, "recipients") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("user_id")

    def listName = column[String]("list_name")

    def userIds = column[List[Long]]("user_ids")

    def default = column[Option[Boolean]]("is_default")

    def createdTimestamp = column[ZonedDateTime]("created_timestamp", SqlUtils.timestampTzNotNullType)

    def modifiedTimestamp = column[ZonedDateTime]("modified_timestamp", SqlUtils.timestampTzNotNullType)

    def * = (id.?, userId, listName, userIds, default, createdTimestamp.?, modifiedTimestamp.?) <> ((Recipient.apply _).tupled, Recipient.unapply)
  }

  protected val recipients = TableQuery[Recipients]

}
