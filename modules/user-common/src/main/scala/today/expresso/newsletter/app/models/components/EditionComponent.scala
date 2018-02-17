package models.components

import java.net.URL

import db.Repository
import models.Edition
import slick.jdbc.GetResult

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
      r.nextDate(),
      r.nextStringOption().map(new URL(_)),
      r.nextStringOption(),
      Option(playJsonTypeMapper.getValue(r.rs, r.skip.currentPos)),
      Option(playJsonTypeMapper.getValue(r.rs, r.skip.currentPos)),
      Option(playJsonTypeMapper.getValue(r.rs, r.skip.currentPos)),
      r.nextTimestamp().toInstant
    )
  }
}
