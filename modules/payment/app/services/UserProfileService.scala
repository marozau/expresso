package services

import java.time.LocalDate

import javax.inject.{Inject, Singleton}
import models.UserProfile
import models.daos.UserProfileDao

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class UserProfileService @Inject()(userProfileDao: UserProfileDao)(implicit ec: ExecutionContext) {

  def getByUserId(userId: Long) = userProfileDao.getByUserId(userId)

  def update(userId: Long,
             status: Option[UserProfile.Status.Value],
             firstName: Option[String],
             lastName: Option[String],
             dateOfBirth: Option[LocalDate],
             country: Option[String],
             city: Option[String],
             postcode: Option[String]) = {
    userProfileDao.update(userId, status, firstName, lastName, dateOfBirth, country, city, postcode)
  }
}
