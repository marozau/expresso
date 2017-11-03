package services

import javax.inject.{Inject, Singleton}

import exceptions.InvalidCampaignStatusException
import models.Campaign
import models.daos.CampaignDao

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class CampaignService @Inject()(campaignDao: CampaignDao)(implicit ec: ExecutionContext) {

  def updateStatus(userId: Long, campaignId: Long, status: Campaign.Status.Value) = {
    campaignDao.updateStatus(userId, campaignId, status)
      .map { res =>
        if (res == 0) throw InvalidCampaignStatusException(userId, campaignId, status, "cannot update status")
        res
      }
  }
}
