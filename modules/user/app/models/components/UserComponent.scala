package models.components

import com.github.tminglei.slickpg.utils.PlainSQLUtils
import db.Repository
import models._
import slick.jdbc.{GetResult, SetParameter}

/**
  * @author im.
  */
trait UserComponent {
  this: Repository =>

  implicit val userRoleTypeMapper = createEnumJdbcType("user_role", User.Role)
  implicit val userRoleListTypeMapper = createEnumListJdbcType("user_role", User.Role)
  implicit val userRoleColumnExtensionMethodsBuilder = createEnumColumnExtensionMethodsBuilder(User.Role)
  implicit val userStatusTypeMapper = createEnumJdbcType("user_status", User.Status)
  implicit val userStatusListTypeMapper = createEnumListJdbcType("user_status", User.Status)
  implicit val userStatusColumnExtensionMethodsBuilder = createEnumColumnExtensionMethodsBuilder(User.Status)

  implicit val setUserRole = PlainSQLUtils.mkSetParameter[User.Role.Value]("user_role")
  implicit val setUserRoleList = PlainSQLUtils.mkArraySetParameter[User.Role.Value]("user_role")

  implicit val getResultUser: GetResult[User] = GetResult { r =>
    User(
      r.nextLong(),
      userStatusTypeMapper.getValue(r.rs, r.skip.currentPos),
      userRoleListTypeMapper.getValue(r.rs, r.skip.currentPos),
      r.nextStringOption(),
      r.nextIntOption(),
      r.nextStringOption(),
      r.nextTimestamp().toInstant)
  }
}
