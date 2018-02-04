package services

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import models.daos.UserDao

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class UserService @Inject()(userDao: UserDao, passwordHasherRegistry: PasswordHasherRegistry)(implicit ec: ExecutionContext) {

  def getByLoginInfo(loginInfo: LoginInfo) = userDao.getByLoginInfo(loginInfo)

  def getById(userId: Long) = userDao.getById(userId)

  def save(email: String, password: String, locale: Option[String], timezone: Option[Int]) = {
    val authInfo = passwordHasherRegistry.current.hash(password)
    userDao.save(email, authInfo.password, authInfo.hasher, locale, timezone)
  }

  def verify(userId: Long, token: UUID) = {
    userDao.verify(userId, token)
  }
}
