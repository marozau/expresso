package services

import java.lang.invoke.MethodHandles
import java.time.Instant

import javax.inject.{Inject, Singleton}
import models.Campaign
import models.daos.CampaignDao
import org.slf4j.LoggerFactory
import play.api.libs.json.JsValue
import services.utils.CampaignScheduler
import today.expresso.stream.ProducerPool

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class CampaignService @Inject()(campaignDao: CampaignDao, campaignScheduler: CampaignScheduler)
                               (implicit ec: ExecutionContext, pp: ProducerPool) {

  import Campaign._

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  def getByEditionId(userId: Long, editionId: Long) = campaignDao.getByEditionId(userId, editionId)

  def createOrUpdate(userId: Long, editionId: Long, sendTime: Instant, preview: Option[String], options: Option[JsValue]) = {
    pp.transaction { producer =>
      logger.info(s"createOrUpdate, userId=$userId, editionId=$editionId")
      campaignDao.createOrUpdate(userId, editionId, sendTime, preview, options) { campaign =>
        producer.send(ToCampaignUpdated(campaign))
      }
    }
  }

  def startCampaign(userId: Long, editionId: Long) = pp.transaction { producer =>
    logger.info(s"startCampaign, userId=$userId, editionId=$editionId")
    campaignDao.start(userId, editionId) { campaign =>
      campaignScheduler.schedule(userId, campaign)
        .map { _ =>
          for {
            _ <- producer.send(ToCampaignUpdated(campaign))
            _ <- producer.send(ToCampaignStarted(campaign))
          } yield ()
        }
    }
  }

  def startSendingCampaign(userId: Long, editionId: Long) = pp.transaction { producer =>
    logger.info(s"startSendingCampaign, userId=$userId, editionId=$editionId")
    campaignDao.startSending(userId, editionId) { campaign =>
      campaignScheduler.startSending(userId, campaign)
        .map(_ => producer.send(ToCampaignUpdated(campaign)))
    }
  }

  def suspendCampaign(userId: Long, editionId: Long) = pp.transaction { producer =>
    logger.info(s"suspendCampaign, userId=$userId, editionId=$editionId")
    campaignDao.suspend(userId, editionId) { campaign =>
      campaignScheduler.suspend(campaign)
        .map(_ => producer.send(ToCampaignUpdated(campaign)))
    }
  }

  def resumeCampaign(userId: Long, editionId: Long) = pp.transaction { producer =>
    logger.info(s"resumeCampaign, userId=$userId, editionId=$editionId")
    campaignDao.resume(userId, editionId) { campaign =>
      campaignScheduler.resume(campaign)
        .map(_ => producer.send(ToCampaignUpdated(campaign)))
    }
  }

  def stopCampaign(userId: Long, editionId: Long) = {
    logger.info(s"stopCampaign, userId=$userId, editionId=$editionId")
  }

  def completeCampaign(userId: Long, editionId: Long, forced: Boolean) = pp.transaction { producer =>
    logger.info(s"completeCampaign, userId=$userId, editionId=$editionId, forced=$forced")
    campaignDao.complete(userId, editionId, forced) { campaign =>
      producer.send(ToCampaignUpdated(campaign))
    }
  }

  def suspendUserCampaigns(userId: Long, forced: Boolean) = pp.transaction { producer =>
    logger.info(s"suspendUserCampaigns, userId=$userId")
    campaignDao.suspendByUser(userId, forced) { campaigns =>
      campaignScheduler.suspendByUser(userId)
        .map(_ => campaigns.map(c => producer.send(ToCampaignUpdated(c))))
    }
  }
}
