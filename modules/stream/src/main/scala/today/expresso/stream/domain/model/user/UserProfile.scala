package today.expresso.stream.domain.model.user

import java.time.LocalDate

import today.expresso.stream.domain.model.user.UserProfile.UserSex.UserSex

/**
  * @author im.
  */
object UserProfile {

  object UserSex extends Enumeration {
    type UserSex = Value
    val MALE, FEMALE, UNKNOWN = Value
  }

}

case class UserProfile(userId: Long,
                       firstName: Option[String],
                       lastName: Option[String],
                       sex: Option[UserSex],
                       dateOfBirth: Option[LocalDate],
                       country: Option[String],
                       city: Option[String],
                       postCode: Option[String],
                       rating: Option[BigDecimal])
