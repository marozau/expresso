package models

import play.api.libs.json.Reads

/**
  * @author im.
  */

object RecipientStatus extends Enumeration {
  val SUBSCRIBED, UNSUBSCRIBED, REMOVED, CLEANED, SPAM = Value

  implicit val recipientStatusFormat = Reads.enumNameReads(RecipientStatus)
}

case class Recipient(userId: Long,
                     email: String,
                     ustatus: UserStatus.Value,
                     rstatus: RecipientStatus.Value)

case class Recipients(listId: Option[Long],
                      userId: Long,
                      name: String,
                      default: Option[Boolean],
                      recipients: Seq[Recipient])

case class RecipientList(id: Option[Long],
                         userId: Long,
                         name: String,
                         default: Option[Boolean])


