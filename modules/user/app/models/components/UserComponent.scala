package models.components

import java.util.UUID

import db.Repository
import models._
import slick.jdbc.{GetResult, SetParameter}

/**
  * @author im.
  */
trait UserComponent {
  this: Repository =>

  import api._

  implicit val userRoleTypeMapper = createEnumJdbcType("user_role", User.Role)
  implicit val userRoleListTypeMapper = createEnumListJdbcType("user_role", User.Role)
  implicit val userRoleColumnExtensionMethodsBuilder = createEnumColumnExtensionMethodsBuilder(User.Role)
  implicit val userStatusTypeMapper = createEnumJdbcType("user_status", User.Status)
  implicit val userStatusListTypeMapper = createEnumListJdbcType("user_status", User.Status)
  implicit val userStatusColumnExtensionMethodsBuilder = createEnumColumnExtensionMethodsBuilder(User.Status)

  implicit val setUserRole: SetParameter[User.Role.Value] = SetParameter { (t, pp) => userRoleTypeMapper.setValue(t, pp.ps, pp.pos + 1) }
  implicit val setUserRoles: SetParameter[List[User.Role.Value]] = SetParameter { (t, pp) => userRoleListTypeMapper.setValue(t, pp.ps, pp.pos + 1) }
  implicit val setUserRoles2: SetParameter[List[Long]] = SetParameter { (t, pp) => simpleLongListTypeMapper.setValue(t, pp.ps, pp.pos + 1) }
  implicit val setUUID: SetParameter[UUID] = SetParameter { (t, pp) => uuidColumnType.setValue(t, pp.ps, pp.pos + 1) }

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
