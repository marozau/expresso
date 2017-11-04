package models

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

case class Recipient(newsletterId: Long,
                     userId: Long,
                     status: Recipient.Status.Value)


