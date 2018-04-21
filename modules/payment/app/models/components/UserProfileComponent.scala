package models.components

import com.github.tminglei.slickpg.utils.PlainSQLUtils
import models.UserProfile
import slick.jdbc.GetResult
import today.expresso.common.db.Repository

/**
  * @author im.
  */
trait UserProfileComponent {
  this: Repository =>

  import api._

  implicit val userStatusTypeMapper = createEnumJdbcType("user_status", UserProfile.Status)
  implicit val userStatusListTypeMapper = createEnumListJdbcType("user_status", UserProfile.Status)
  implicit val userStatusColumnExtensionMethodsBuilder = createEnumColumnExtensionMethodsBuilder(UserProfile.Status)

  implicit val userStatusSet = PlainSQLUtils.mkSetParameter[UserProfile.Status.Value]("user_status")
  implicit val userStatusOptionSet = PlainSQLUtils.mkOptionSetParameter[UserProfile.Status.Value]("user_status")

  implicit val getResultUser: GetResult[UserProfile] = GetResult { r =>
    UserProfile(
      r.nextLong(),
      Option(userStatusTypeMapper.getValue(r.rs, r.skip.currentPos)),
      r.nextStringOption(),
      r.nextStringOption(),
      r.nextLocalDateOption(),
      r.nextStringOption(),
      r.nextStringOption(),
      r.nextStringOption()
    )
  }
}
