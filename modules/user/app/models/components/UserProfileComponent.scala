package models.components

import db.Repository
import models.UserSex

/**
  * @author im.
  */
trait UserProfileComponent {
  this: Repository with UserComponent =>

  implicit val userSexTypeMapper = createEnumJdbcType("user_sex", UserSex)
  implicit val userSexListTypeMapper = createEnumListJdbcType("user_sex", UserSex)
  implicit val userSexColumnExtentionMethodsBuilder = createEnumColumnExtensionMethodsBuilder(UserSex)

}
