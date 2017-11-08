package models.daos

import javax.inject.{Inject, Singleton}

import exceptions.{CampaignNotFoundException, InvalidCampaignStatusException}
import models.Campaign
import models.api.Repository
import models.components.{CampaignComponent, EditionComponent, NewsletterComponent, UserComponent}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class CampaignDao @Inject()(databaseConfigProvider: DatabaseConfigProvider,
                            newsletterDao: EditionDao,
                           )(implicit ec: ExecutionContext)
  extends Repository with CampaignComponent with EditionComponent with UserComponent with NewsletterComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  implicit def campaignCast(db: DBCampaign): Campaign = Campaign(db.id, db.newsletterId, db.editionId, db.preview, db.sendTime, db.status, db.options)

  implicit def dbCampaignCast(c: Campaign): DBCampaign = DBCampaign(c.id, c.newsletterId, c.editionId, c.preview, c.sendTime, c.status, c.options)

  //TODO: compose create and update into one method
  def save(campaign: Campaign): Future[Campaign] = {
    campaign.id.fold(create(campaign))(_ => update(campaign).map(_ => campaign))
  }

  def create(campaign: Campaign): Future[Campaign] = db.run {
    ((campaigns returning campaigns) += campaign.copy(status = Campaign.Status.NEW))
      .map(campaignCast)
  }


  def getById(id: Long) = db.run {
    campaigns.filter(_.id === id).result.headOption.map(_.map(campaignCast))
  }

  //TODO: thinks about campaign status. is it possible to change campaign when it is scheduled or sent or else???
  def update(campaign: Campaign): Future[Int] = {
    val updateQuery = campaigns.filter(c => c.id === campaign.id && c.status === Campaign.Status.NEW)
      .map(c => (c.preview, c.sendTime, c.options))
      .update(campaign.preview, campaign.sendTime, campaign.options)
    db.run(updateQuery.transactionally.withPinnedSession)
  }

  def getByEditionId(editionId: Long): Future[Option[Campaign]] = db.run {
    campaigns.filter(_.editionId === editionId).result.headOption
      .map(_.map(campaignCast))
  }

  def getLastSent(): Future[Campaign] = db.run {
    campaigns
      .filter(c => c.status === Campaign.Status.SENT)
      .sortBy(_.sendTime)
      .take(1)
      .result.head
      .map(campaignCast)
  }

  def setPendingStatus(campaignId: Long) = db.run {
    campaigns
      .filter(c => c.id === campaignId && c.status.inSet(List(Campaign.Status.NEW, Campaign.Status.PENDING)))
      .map(_.status)
      .update(Campaign.Status.PENDING)
      .transactionally
  }

  def setSendingStatus(campaignId: Long) = db.run {
    campaigns
      .filter(c => c.id === campaignId && c.status === Campaign.Status.PENDING)
      .map(_.status)
      .update(Campaign.Status.SENDING)
      .transactionally
  }

  def setSentStatus(campaignId: Long) = db.run {
    campaigns
      .filter(c => c.id === campaignId && c.status === Campaign.Status.SENDING)
      .map(_.status)
      .update(Campaign.Status.SENT)
      .transactionally
  }

  /**
    * Used for recovery operations, in normal cases more specialised methods must be used (aka setPendingStatus, setSendingStatus etc)
    *
    * @param campaignId
    * @param status
    * @return
    */
  def updateStatus(campaignId: Long, status: Campaign.Status.Value) = db.run {
    campaigns
      .filter(_.id === campaignId)
      .map(_.status)
      .update(status)
      .transactionally
  }
}
