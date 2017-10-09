package repositories

import java.time.{LocalDate, ZonedDateTime}

import models.{UserProfile, UserSex}

/**
  * @author im.
  */
trait UserProfileComponent {
  this: Repository with UserComponent =>

  import api._

  implicit val userSexTypeMapper = createEnumJdbcType("user_sex", UserSex)
  implicit val userSexListTypeMapper = createEnumListJdbcType("user_sex", UserSex)
  implicit val userSexColumnExtentionMethodsBuilder = createEnumColumnExtensionMethodsBuilder(UserSex)

  protected class UserProfiles(tag: Tag) extends Table[UserProfile](tag, "user_profiles") {
    def userId = column[Long]("user_id", O.PrimaryKey)

    def firstName = column[Option[String]]("first_name")

    def lastName = column[Option[String]]("last_name")

    def sex = column[Option[UserSex.Value]]("sex")

    def dateOfBirth = column[Option[LocalDate]]("date_of_birth")

    def country = column[Option[String]]("country")

    def city = column[Option[String]]("city")

    def postcode = column[Option[String]]("postcode")

    def timezone = column[Option[String]]("timezone")

    def modifiedTimestamp = column[ZonedDateTime]("modified_timestamp", O.Default(ZonedDateTime.now()))

    def * = (userId, firstName, lastName, sex, dateOfBirth, country, city, postcode, timezone, modifiedTimestamp) <> ((UserProfile.apply _).tupled, UserProfile.unapply)

    def supplier = foreignKey("user_id_fk", userId, users)(_.id)
  }

  protected val userProfiles = TableQuery[UserProfiles]

}
