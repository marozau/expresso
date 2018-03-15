package services

import java.lang.invoke.MethodHandles
import java.time.Instant
import javax.inject.{Inject, Singleton}

import models.Campaign
import models.daos.CampaignDao
import org.slf4j.LoggerFactory
import play.api.libs.json.JsValue
import today.expresso.common.utils.Tx
import utils.CampaignScheduler

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class CampaignService @Inject()(campaignDao: CampaignDao, campaignScheduler: CampaignScheduler)(implicit ec: ExecutionContext) {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  implicit val tx: Tx[Campaign] = c => Future.successful(c)

  def getByEditionId(userId: Long, editionId: Long) = campaignDao.getByEditionId(userId, editionId)

  def createOrUpdate(userId: Long, editionId: Long, sendTime: Instant, preview: Option[String], options: Option[JsValue]) = {
    logger.info(s"createOrUpdate, userId=$userId, editionId=$editionId")
    campaignDao.createOrUpdate(userId, editionId, sendTime, preview, options) //TODO: event
  }

  def startCampaign(userId: Long, editionId: Long) = {
    logger.info(s"startCampaign, userId=$userId, editionId=$editionId")
    campaignDao.start(userId, editionId) { campaign =>
      campaignScheduler.schedule(userId, campaign)
    }
  }

  def startSendingCampaign(userId: Long, editionId: Long) = {
    logger.info(s"startSendingCampaign, userId=$userId, editionId=$editionId")
    campaignDao.startSending(userId, editionId) { campaign =>
      campaignScheduler.startSending(userId, campaign)
    }
  }

  def suspendCampaign(userId: Long, editionId: Long) = {
    logger.info(s"suspendCampaign, userId=$userId, editionId=$editionId")
    campaignDao.suspend(userId, editionId) { campaign =>
      campaignScheduler.suspend(campaign)
    }
  }

  def resumeCampaign(userId: Long, editionId: Long) = {
    logger.info(s"resumeCampaign, userId=$userId, editionId=$editionId")
    campaignDao.resume(userId, editionId) { campaign =>
      campaignScheduler.resume(campaign)
    }
  }

  def stopCampaign(userId: Long, editionId: Long) = {
    logger.info(s"stopCampaign, userId=$userId, editionId=$editionId")
  }

  def completeCampaign(userId: Long, editionId: Long, forced: Boolean) = {
    logger.info(s"completeCampaign, userId=$userId, editionId=$editionId, forced=$forced")
    campaignDao.complete(userId, editionId, forced) //TODO: event
  }

  def suspendUserCampaigns(userId: Long, forced: Boolean) = {
    logger.info(s"suspendUserCampaigns, userId=$userId")
    campaignDao.suspendByUser(userId, forced) { campaigns =>
      campaignScheduler.suspendByUser(userId).map(_ => campaigns)
    } //TODO: event
  }
}
