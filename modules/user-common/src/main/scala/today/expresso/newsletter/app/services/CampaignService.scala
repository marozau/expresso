package services

import javax.inject.{Inject, Singleton}

import today.expresso.common.exceptions.CampaignNotFoundException
import models.Campaign
import models.daos.CampaignDao

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class CampaignService @Inject()(campaignDao: CampaignDao)(implicit ec: ExecutionContext) {

  def save(campaign: Campaign) = {
    def ifEmpty() = campaignDao.create(campaign)

    def ifExist(campaignId: Long) = {
      campaignDao.update(campaign)
        .map { result =>
          if (result == 0) throw CampaignNotFoundException("save failed")
          campaign
        }

    }

    campaign.id.fold(ifEmpty())(ifExist)
  }

  def getById(campaignId: Long) = {
    campaignDao.getById(campaignId)
      .map { result =>
        if (result.isEmpty) throw CampaignNotFoundException(s"getById failed")
        result.get
      }
  }

  def getByEditionId(editionId: Long) = campaignDao.getByEditionId(editionId)

  def setPendingStatus(campaignId: Long) = {
    campaignDao.setPendingStatus(campaignId)
      .map { res =>
        if (res == 0) throw CampaignNotFoundException(s"setPendingStatus failed")
        res
      }
  }

  def setSendingStatus(campaignId: Long) = {
    campaignDao.setSendingStatus(campaignId)
      .map { res =>
        if (res == 0) throw CampaignNotFoundException(s"setSendingStatus failed")
        res
      }
  }

  def setSentStatus(campaignId: Long) = {
    campaignDao.setSentStatus(campaignId)
      .map { res =>
        if (res == 0) throw CampaignNotFoundException(s"setSentStatus failed")
        res
      }
  }

  def updateStatus(campaignId: Long, status: Campaign.Status.Value) = {
    campaignDao.updateStatus(campaignId, status)
  }
}
