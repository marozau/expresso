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

  //TODO: compose create and update into one method
  def save(campaign: Campaign): Future[Campaign] = {
    campaign.id.fold(create(campaign))(_ => update(campaign).map(_ => campaign))
  }

  def create(campaign: Campaign): Future[Campaign] = db.run {
    ((campaigns returning campaigns) += DBCampaign(None, campaign.editionId, campaign.preview, campaign.sendTime, Campaign.Status.NEW, campaign.options))
      .map(db => campaign.copy(id = db.id, status = db.status))
  }

  def getByIdDBIO(id: Long): DBIOAction[Campaign, NoStream, Effect.Read] = {
    campaigns.filter(_.id === id).result.map {
      p =>
        if (p.isEmpty) throw CampaignNotFoundException(id, s"getByIdDBIO failed")
        val db = p.head
        Campaign(db.id,
          db.editionId,
          db.preview,
          db.sendTime,
          db.status,
          db.options)
    }
  }

  def getById(id: Long): Future[Campaign] = db.run(getByIdDBIO(id))

  def updateDBIO(campaign: Campaign) = {
    getByIdDBIO(campaign.id.get)
      .flatMap { dbCampaign =>
        val q = for (p <- campaigns if p.id === campaign.id) yield p
        // update status only with separate procedure
        q.update(DBCampaign(campaign.id, campaign.editionId, campaign.preview, campaign.sendTime, dbCampaign.status, campaign.options))
      }
      .transactionally.withPinnedSession
  }

  def update(campaign: Campaign): Future[Int] = db.run {
    updateDBIO(campaign)
  }

  def getByEditionId(editionId: Long): Future[Option[Campaign]] = db.run {
    campaigns.filter(_.editionId === editionId).result.headOption
      .map(_.map(db => Campaign(db.id, db.editionId, db.preview, db.sendTime, db.status, db.options)))
  }

  def getLastSent(): Future[Campaign] = db.run {
    campaigns
      .filter(c => c.status === Campaign.Status.SENT)
      .sortBy(_.sendTime)
      .take(1)
      .result.head
      .map(db => Campaign(db.id, db.editionId, db.preview, db.sendTime, db.status, db.options))
  }

  def setPendingStatus(campaignId: Long, status: Campaign.Status.Value) = db.run {
    campaigns
      .filter(c => c.id === campaignId && c.status =!= Campaign.Status.SENT)
      .map(_.status)
      .update(status)
      .map { res =>
        if (res == 0) throw InvalidCampaignStatusException(campaignId, status, "cannot update status")
        res
      }
      .transactionally
  }

  def updateStatus(campaignId: Long, status: Campaign.Status.Value) = db.run {
    campaigns
      .filter(_.id === campaignId)
      .map(_.status)
      .update(status)
      .map { res =>
        if (res == 0) throw InvalidCampaignStatusException(campaignId, status, "cannot update status")
        res
      }
      .transactionally
  }
}
