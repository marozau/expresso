package models.components

import java.time.ZonedDateTime

import models.Campaign
import models.api.Repository
import play.api.libs.json.JsValue
import utils.SqlUtils

/**
  * @author im.
  */
trait CampaignComponent {
  this: Repository with NewsletterComponent =>

  import api._

  implicit val campaignStatusMapper = createEnumJdbcType("campaign_status", Campaign.Status)

  protected class Campaigns(tag: Tag) extends Table[Campaign](tag, "campaigns") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("user_id")

    def newsletterId = column[Long]("newsletter_id")

    def name = column[String]("name")

    def subject = column[String]("subject")

    def preview = column[Option[String]]("preview")

    def fromName = column[String]("from_name")

    def fromEmail = column[String]("from_email")

    def sendTime = column[ZonedDateTime]("send_time")

    def recipientId = column[Long]("recipient_id")

    def status = column[Campaign.Status.Value]("status")

    def emailSent = column[Int]("email_sent")

    def options = column[Option[JsValue]]("options")

    def createdTimestamp = column[ZonedDateTime]("created_timestamp", SqlUtils.timestampTzNotNullType)

    def modifiedTimestamp = column[ZonedDateTime]("modified_timestamp", SqlUtils.timestampTzNotNullType)

    def * = (id.?, userId, newsletterId, name, subject, preview, fromName, fromEmail, sendTime, recipientId, status, emailSent, options, createdTimestamp.?, modifiedTimestamp.?) <>
      ((Campaign.apply _).tupled, Campaign.unapply)

    def newsletterSupplier = foreignKey("campaign_newsletter_id_fkey", newsletterId, newsletters)(_.id)
  }

  protected val campaigns = TableQuery[Campaigns]

}
