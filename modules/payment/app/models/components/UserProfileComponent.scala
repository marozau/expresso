package models.components

import models.UserProfile
import slick.jdbc.GetResult
import today.expresso.common.db.Repository

/**
  * @author im.
  */
trait UserProfileComponent {
  this: Repository =>

  import api._

  implicit val getResultUser: GetResult[UserProfile] = GetResult { r =>
    UserProfile(
      r.nextLong(),
      r.nextStringOption(),
      r.nextStringOption(),
      r.nextLocalDateOption(),
      r.nextStringOption(),
      r.nextStringOption(),
      r.nextStringOption()
    )
  }
}
