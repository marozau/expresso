package models

import java.time.LocalDate

object UserProfile {

  object Status extends Enumeration {
    val NEW, VERIFIED, BLOCKED = Value
  }

}

case class UserProfile(userId: Long,
                       status: Option[UserProfile.Status.Value],
                       firstName: Option[String],
                       lastName: Option[String],
                       dateOfBirth: Option[LocalDate],
                       country: Option[String],
                       city: Option[String],
                       postCode: Option[String])

