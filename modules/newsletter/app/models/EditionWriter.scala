package models

import java.time.Instant
import java.util.UUID

/**
  * @author im.
  */
case class EditionWriter(id: UUID,
                         editionId: Long,
                         userId: Long,
                         createdTimestamp: Instant)
