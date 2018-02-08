package services

import javax.inject.{Inject, Singleton}

import models.daos.UserProfileDao

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class UserProfileService @Inject()(userProfileDao: UserProfileDao)(implicit ec: ExecutionContext) {

  def getByUserId(userId: Long) = userProfileDao.getByUserId(userId)
}
