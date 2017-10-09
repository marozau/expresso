package models

import java.time.{LocalDate, ZonedDateTime}

import play.api.libs.json.{Json, Reads}

/**
  * @author im.
  */
object UserSex extends Enumeration {
  val MALE, FEMALE, UNKNOWN = Value

  implicit val userSexReads = Reads.enumNameReads(UserSex)
}

case class UserProfile(
                        userId: Long,
                        firstName: Option[String],
                        lastName: Option[String],
                        sex: Option[UserSex.Value],
                        dateOfBirth: Option[LocalDate],
                        country: Option[String],
                        city: Option[String],
                        postCode: Option[String],
                        timeZone: Option[String],
                        modifiedTimestamp: ZonedDateTime
                      )

object UserProfile {
  implicit val userProfileFormat = Json.format[UserProfile]
}
