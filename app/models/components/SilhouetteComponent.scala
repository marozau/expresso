package models.components

import models.api.Repository

/**
  * @author im.
  */
trait SilhouetteComponent {
  this: Repository with UserComponent =>

  import api._


  case class DBLoginInfo(
                          id: Option[Long],
                          providerId: String,
                          providerKey: String
                        )

  class LoginInfos(tag: Tag) extends Table[DBLoginInfo](tag, "login_info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def providerId = column[String]("provider_id")

    def providerKey = column[String]("provider_key")

    def * = (id.?, providerId, providerKey) <> (DBLoginInfo.tupled, DBLoginInfo.unapply)
  }

  case class DBUserLoginInfo(
                              userId: Long,
                              loginInfoId: Long
                            )

  class UserLoginInfos(tag: Tag) extends Table[DBUserLoginInfo](tag, "user_login_info") {
    def userId = column[Long]("user_id")

    def loginInfoId = column[Long]("login_info_id")

    def * = (userId, loginInfoId) <> (DBUserLoginInfo.tupled, DBUserLoginInfo.unapply)

    def userIdSupplier = foreignKey("user_login_info_user_id_fkey", userId, users)(_.id)
    def loginInfoIdSupplier = foreignKey("user_login_info_login_info_id_fkey", loginInfoId, loginInfos)(_.id)
  }

  case class DBPasswordInfo(
                             hasher: String,
                             password: String,
                             salt: Option[String],
                             loginInfoId: Long
                           )

  class PasswordInfos(tag: Tag) extends Table[DBPasswordInfo](tag, "password_info") {
    def loginInfoId = column[Long]("login_info_id")

    def hasher = column[String]("hasher")

    def password = column[String]("password")

    def salt = column[Option[String]]("salt")

    def * = (hasher, password, salt, loginInfoId) <> (DBPasswordInfo.tupled, DBPasswordInfo.unapply)

    def loginInfoIdSupplier = foreignKey("password_info_login_info_id_fkey", loginInfoId, loginInfos)(_.id)
  }

  val loginInfos = TableQuery[LoginInfos]
  val userLoginInfos = TableQuery[UserLoginInfos]
  val passwordInfos = TableQuery[PasswordInfos]

  import com.mohiva.play.silhouette.api.LoginInfo
  def loginInfoQuery(loginInfo: LoginInfo) =
    loginInfos.filter(dbLoginInfo => dbLoginInfo.providerId === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey)
}
