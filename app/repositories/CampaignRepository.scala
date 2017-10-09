package repositories

import javax.inject.{Inject, Singleton}

import exceptions.CampaignNotFoundException
import models.Campaign
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class CampaignRepository @Inject()(databaseConfigProvider: DatabaseConfigProvider,
                                   newsletterRepo: NewsletterRepository,
                                  )(implicit ec: ExecutionContext)
  extends Repository with CampaignComponent with NewsletterComponent with UserComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def create(campaign: Campaign): Future[Campaign] = db.run {
    {
      for {
        c <- (campaigns returning campaigns) += campaign
        _ <- newsletterRepo.updateTitleDBIO(campaign.userId, campaign.newsletterId, campaign.subject)
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

  def updateDBIO(campaign: Campaign): DBIOAction[Int, NoStream, Effect.Write] = {
    val q = for (p <- campaigns if p.id === campaign.id && p.userId === campaign.userId) yield p
    q.update(campaign)
      .map { updated =>
        if (updated == 0) throw CampaignNotFoundException(campaign.id.get, s"campaign update failed")
        updated
      }
  }

  def update(campaign: Campaign): Future[Int] = db.run {
    updateDBIO(campaign)
      .flatMap(_ => newsletterRepo.updateTitleDBIO(campaign.userId, campaign.newsletterId, campaign.subject))
      .transactionally.withPinnedSession
  }

  def getByNewsletterId(userId: Long, newsletterId: Long): Future[Option[Campaign]] = db.run {
    campaigns.filter(c => c.userId === userId && c.newsletterId === newsletterId).result.headOption
  }

}
