package models

import java.util.UUID

import play.api.libs.json.Reads

/**
  * @author im.
  */

object Recipient {

  object Status extends Enumeration {
    val PENDING, SUBSCRIBED, UNSUBSCRIBED, REMOVED, CLEANED, SPAM = Value

    implicit val recipientStatusFormat = Reads.enumNameReads(Status)
  }

}

case class Recipient(id: Option[UUID],
                     newsletterId: Long,
                     userId: Long,
                     status: Recipient.Status.Value)


