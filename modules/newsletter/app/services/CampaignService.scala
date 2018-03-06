package services

import java.time.Instant
import javax.inject.{Inject, Singleton}

import models.Campaign
import models.daos.CampaignDao
import play.api.libs.json.JsValue
import utils.Tx

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class CampaignService @Inject()(campaignDao: CampaignDao, campaignSchedulerService: CampaignSchedulerService)(implicit ec: ExecutionContext) {

  implicit val tx: Tx[Campaign] = c => Future.successful(c)

  def getByEditionId(userId: Long, editionId: Long) = campaignDao.getByEditionId(userId, editionId)

  def createOrUpdate(userId: Long, editionId: Long, sendTime: Instant, preview: Option[String], options: Option[JsValue]) = {
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
    campaignDao.setSuspendedStatus(userId, editionId) //TODO: event
  }

  def scheduleCampaign(userId: Long, editionId: Long) = {
    campaignDao.setPendingStatus(userId, editionId) { campaign =>
      campaignSchedulerService.scheduleCampaign(userId, campaign)
    }
  }

  def scheduleEdition(editionId: Long) = {
    campaignDao.setSendingStatus(editionId) { campaign =>
      campaignSchedulerService.scheduleEdition(campaign)
    }
  }

  def suspendCampaign() = {
  }

  def removeCampaign() = {

  }
}
