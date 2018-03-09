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
class CampaignService @Inject()(campaignDao: CampaignDao, campaignSchedulerService: CampaignScheduler)(implicit ec: ExecutionContext) {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  implicit val tx: Tx[Campaign] = c => Future.successful(c)

  def getByEditionId(userId: Long, editionId: Long) = campaignDao.getByEditionId(userId, editionId)

  def createOrUpdate(userId: Long, editionId: Long, sendTime: Instant, preview: Option[String], options: Option[JsValue]) = {
    logger.info(s"createOrUpdate, userId=$userId, editionId=$editionId")
    campaignDao.createOrUpdate(userId, editionId, sendTime, preview, options) //TODO: event
  }

  def setPendingStatus(userId: Long, editionId: Long) = {
    campaignDao.setPendingStatus(userId, editionId) //TODO: event
  }

  def setSendingStatus(editionId: Long) = {
    campaignDao.setSendingStatus(editionId) //TODO: event
  }

  def setSentStatus(editionId: Long) = {
    campaignDao.setSentStatus(editionId) //TODO: event
  }

  def setSuspendedStatus(userId: Long, editionId: Long) = {
    campaignDao.suspend(userId, editionId) //TODO: event
  }

  def startCampaign(userId: Long, editionId: Long) = {
    logger.info(s"startCampaign, userId=$userId, editionId=$editionId")
    campaignDao.setPendingStatus(userId, editionId) { campaign =>
      campaignSchedulerService.schedule(userId, campaign)
    }
  }

  def startSending(userId: Long, editionId: Long) = {
    logger.info(s"startSending, userId=$userId, editionId=$editionId")
    campaignDao.setSendingStatus(editionId) { campaign =>
      campaignSchedulerService.scheduleSending(userId, campaign)
    }
  }

  def suspendCampaign(userId: Long, editionId: Long) = {
    logger.info(s"suspendCampaign, userId=$userId, editionId=$editionId")
    campaignDao.suspend(userId, editionId) { campaign =>
      campaignSchedulerService.suspend(campaign)
    }
  }

  def resumeCampaign(userId: Long, editionId: Long) = {
    logger.info(s"resumeCampaign, userId=$userId, editionId=$editionId")
    campaignDao.resume(userId, editionId) { campaign =>
      campaignSchedulerService.resume(campaign)
    }
  }

  def stopCampaign() = {
  }
}
