package models.components

import today.expresso.common.db.Repository
import models.Post
import slick.jdbc.GetResult

/**
  * @author im.
  */
trait PostComponent {
  this: Repository =>

  import api._

  implicit val postGetResult: GetResult[Post] = GetResult { r =>
    Post(
      r.nextLong(),
      r.nextLong(),
      r.nextLong(),
      r.nextInt(),
      r.nextString(),
      r.nextString(),
      playJsonTypeMapper.getValue(r.rs, r.skip.currentPos),
      Option(playJsonTypeMapper.getValue(r.rs, r.skip.currentPos)),
      r.nextTimestamp().toInstant
    )
  }
}
