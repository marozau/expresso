package models.components

import today.expresso.common.db.Repository
import models.AuthToken
import slick.jdbc.GetResult

/**
  * @author im.
  */
trait AuthTokenComponent {
  this: Repository =>

  import api._

  implicit val getResultAuthToken: GetResult[AuthToken] = GetResult { r =>
    AuthToken(
      uuidColumnType.getValue(r.rs, r.skip.currentPos),
      r.nextLong(),
      r.nextTimestamp().toInstant
    )
  }
}
