package models.components

import java.net.URL

import today.expresso.common.db.Repository
import slick.jdbc.GetResult
import today.expresso.stream.domain.model.newsletter.Edition

/**
  * @author im.
  */

trait EditionComponent {
  this: Repository =>

  import api._

  implicit val editionGetResult: GetResult[Edition] = GetResult { r =>
    Edition(
      r.nextLong(),
      r.nextLong(),
      r.nextDate().toLocalDate,
      r.nextStringOption().map(new URL(_)),
      r.nextStringOption(),
      Option(playJsonTypeMapper.getValue(r.rs, r.skip.currentPos)),
      Option(playJsonTypeMapper.getValue(r.rs, r.skip.currentPos)),
      Option(playJsonTypeMapper.getValue(r.rs, r.skip.currentPos)),
      r.nextTimestamp().toInstant
    )
  }
}
