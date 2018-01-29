package models.components

import java.time.ZonedDateTime

import db.Repository
import models.Campaign
import play.api.libs.json.JsValue
import utils.SqlUtils

/**
  * @author im.
  */
trait CampaignComponent {
  this: Repository =>

  import api._

  implicit val campaignStatusMapper = createEnumJdbcType("campaign_status", Campaign.Status)

  case class DBCampaign(id: Option[Long],
                        newsletterId: Long,
                        editionId: Long,
                        preview: Option[String],
                        sendTime: ZonedDateTime,
                        status: Campaign.Status.Value,
                        options: Option[JsValue] = None,
                        createdTimestamp: Option[ZonedDateTime] = None,
                        modifiedTimestamp: Option[ZonedDateTime] = None)

  protected class Campaigns(tag: Tag) extends Table[DBCampaign](tag, "campaigns") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def newsletterId = column[Long]("newsletter_id")

    def editionId = column[Long]("edition_id")

    def preview = column[Option[String]]("preview")

    def sendTime = column[ZonedDateTime]("send_time")

    def status = column[Campaign.Status.Value]("status")

    def options = column[Option[JsValue]]("options")

    def createdTimestamp = column[ZonedDateTime]("created_timestamp", SqlUtils.timestampTzNotNullType)

    def modifiedTimestamp = column[ZonedDateTime]("modified_timestamp", SqlUtils.timestampTzNotNullType)

    def * = (id.?, newsletterId, editionId, preview, sendTime, status, options, createdTimestamp.?, modifiedTimestamp.?) <> ((DBCampaign.apply _).tupled, DBCampaign.unapply)

  }

  protected val campaigns = TableQuery[Campaigns]

}
