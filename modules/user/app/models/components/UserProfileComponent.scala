package models.components

import today.expresso.common.db.Repository
import models.{UserProfile, UserSex}
import slick.jdbc.{GetResult, JdbcType}

/**
  * @author im.
  */
trait UserProfileComponent {
  this: Repository =>

  import api._

  implicit val userSexTypeMapper: JdbcType[UserSex.Value] = createEnumJdbcType("user_sex", UserSex)
  implicit val userSexListTypeMapper: JdbcType[List[UserSex.Value]] = createEnumListJdbcType("user_sex", UserSex)
  implicit val userSexColumnExtensionMethodsBuilder = createEnumColumnExtensionMethodsBuilder(UserSex)

  implicit val getResultUser: GetResult[UserProfile] = GetResult { r =>
    UserProfile(
      r.nextLong(),
      r.nextStringOption(),
      r.nextStringOption(),
      Option(userSexTypeMapper.getValue(r.rs, r.skip.currentPos)),
      r.nextLocalDateOption(),
      r.nextStringOption(),
      r.nextStringOption(),
      r.nextStringOption(),
      r.nextBigDecimalOption()
    )
  }
}
