package models.components

import java.time.ZonedDateTime

import models.api.Repository
import utils.SqlUtils

/**
  * @author im.
  */
trait RecipientListComponent {
  this: Repository with UserComponent =>


  import api._

  case class DBRecipientList(id: Option[Long],
                             userId: Long,
                             name: String,
                             default: Option[Boolean] = None,
                             createdTimestamp: Option[ZonedDateTime] = None,
                             modifiedTimestamp: Option[ZonedDateTime] = None)

  protected class Lists(tag: Tag) extends Table[DBRecipientList](tag, "recipient_lists") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("user_id")

    def name = column[String]("name")

    def default = column[Option[Boolean]]("is_default")

    def createdTimestamp = column[ZonedDateTime]("created_timestamp", SqlUtils.timestampTzNotNullType)

    def modifiedTimestamp = column[ZonedDateTime]("modified_timestamp", SqlUtils.timestampTzNotNullType)

    def * = (id.?, userId, name, default, createdTimestamp.?, modifiedTimestamp.?) <> ((DBRecipientList.apply _).tupled, DBRecipientList.unapply)

    def userIdSupplier = foreignKey("lists_user_id_fkey", userId, users)(_.id)
  }

  protected val rlists = TableQuery[Lists]

}
