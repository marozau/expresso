package services

import javax.inject.{Inject, Singleton}

import exceptions.{CampaignNotFoundException, InvalidCampaignStatusException}
import models.Campaign
import models.daos.CampaignDao

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class CampaignService @Inject()(campaignDao: CampaignDao)(implicit ec: ExecutionContext) {

  def save(campaign: Campaign) = campaignDao.save(campaign)

  def getById(campaignId: Long) = {
    campaignDao.getById(campaignId)
      .map { result =>
        if (result.isEmpty) throw CampaignNotFoundException(campaignId, s"getById failed")
        result.get
      }
  }

  def getByEditionId(editionId: Long) = campaignDao.getByEditionId(editionId)

  def setPendingStatus(campaignId: Long) = {
    campaignDao.setPendingStatus(campaignId)
      .map { res =>
        if (res == 0) throw CampaignNotFoundException(campaignId, s"setPendingStatus failed")
        res
      }
  }

  def setSendingStatus(campaignId: Long) = {
    campaignDao.setSendingStatus(campaignId)
      .map { res =>
        if (res == 0) throw CampaignNotFoundException(campaignId, s"setSendingStatus failed")
        res
      }
  }

  def setSentStatus(campaignId: Long) = {
    campaignDao.setSentStatus(campaignId)
      .map { res =>
        if (res == 0) throw CampaignNotFoundException(campaignId, s"setSentStatus failed")
        res
      }
  }

  def updateStatus(campaignId: Long, status: Campaign.Status.Value) = {
    campaignDao.updateStatus(campaignId, status)
  }
}
