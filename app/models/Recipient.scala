package models

import java.time.ZonedDateTime

/**
  * @author im.
  */
case class Recipient(id: Option[Long],
                     userId: Long,
                     listName: String,
                     userIds: List[Long],
                     default: Option[Boolean],
                     createdTimestamp: Option[ZonedDateTime] = None,
                     modifiedTimestamp: Option[ZonedDateTime] = None)
