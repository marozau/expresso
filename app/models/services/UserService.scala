package models.services

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import exceptions.EmailNotFoundException
import models.{User, UserRole, UserStatus}
import models.daos.UserDao

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class UserService @Inject()(userDao: UserDao)(implicit ec: ExecutionContext)
  extends IdentityService[User] {

  override def retrieve(loginInfo: LoginInfo) = userDao.getByLoginInfo(loginInfo)

  def retrieve(userId: Long) = userDao.getById(userId)

  def save(user: User) = userDao.save(user)

  def save(profile: CommonSocialProfile): Future[User] = {
    userDao.getByLoginInfo(profile.loginInfo).flatMap {
      case Some(user) =>
        userDao.save(user.copy(loginInfo = profile.loginInfo))
      case None =>
        if (profile.email.isDefined) throw EmailNotFoundException("CommonSocialProfile email is empty")
        userDao.save(User(None, profile.loginInfo, profile.email.get, List(UserRole.USER), UserStatus.NEW))
      //TODO: update personal info
    }
  }
}
