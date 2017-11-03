package models.daos

import javax.inject.{Inject, Singleton}

import exceptions.CampaignNotFoundException
import models.Campaign
import models.api.Repository
import models.components.{CampaignComponent, NewsletterComponent, UserComponent}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class CampaignDao @Inject()(databaseConfigProvider: DatabaseConfigProvider,
                            newsletterDao: NewsletterDao,
                           )(implicit ec: ExecutionContext)
  extends Repository with CampaignComponent with NewsletterComponent with UserComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  //TODO: compose create and update into one method
  def save(campaign: Campaign): Future[Campaign] = {
    campaign.id.fold(create(campaign))(_ => update(campaign).map(_ => campaign))
  }

  def create(campaign: Campaign): Future[Campaign] = db.run {
    {
      for {
        c <- (campaigns returning campaigns) += campaign.copy(status = Campaign.Status.NEW)
        _ <- newsletterDao.updateTitleDBIO(campaign.userId, campaign.newsletterId, campaign.subject)
        _ <- newsletterDao.updatePublishTimestampDBIO(campaign.userId, campaign.newsletterId, campaign.sendTime)
      } yield c
    }.transactionally.withPinnedSession
  }

  def getByIdDBIO(id: Long): DBIOAction[Campaign, NoStream, Effect.Read] = {
    campaigns.filter(_.id === id).result.map {
      p =>
        if (p.isEmpty) throw CampaignNotFoundException(id, s"campaign not found")
        p.head
    }
  }

  def getById(id: Long): Future[Campaign] = db.run(getByIdDBIO(id))

  def updateDBIO(campaign: Campaign) = {
    getByIdDBIO(campaign.id.get)
      .flatMap { dbCampaign =>
        val q = for (p <- campaigns if p.id === campaign.id && p.userId === campaign.userId) yield p
        // update status only with separate procedure
        q.update(campaign.copy(status = dbCampaign.status))
      }
      .flatMap { _ =>
        newsletterDao.updateTitleDBIO(campaign.userId, campaign.newsletterId, campaign.subject)
      }
      .flatMap { _ =>
        newsletterDao.updatePublishTimestampDBIO(campaign.userId, campaign.newsletterId, campaign.sendTime)
      }
      .transactionally.withPinnedSession
  }

  def update(campaign: Campaign): Future[Int] = db.run {
    updateDBIO(campaign)
  }

  def getByNewsletterId(userId: Long, newsletterId: Long): Future[Option[Campaign]] = db.run {
    campaigns.filter(c => c.userId === userId && c.newsletterId === newsletterId).result.headOption
  }

  def getLastSent(): Future[Campaign] = db.run {
    campaigns
      .filter(c => c.status === Campaign.Status.SENT)
      .sortBy(_.sendTime)
      .take(1)
      .result.head
  }

  def setPendingStatus(userId: Long, campaignId: Long, status: Campaign.Status.Value) = db.run {
    campaigns.filter(c => c.userId === userId && c.id === campaignId && c.status =!= Campaign.Status.SENT).map(_.status)
      .update(status)
      .transactionally
  }
}
