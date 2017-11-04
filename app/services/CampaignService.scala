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

  def updateStatus(campaignId: Long, status: Campaign.Status.Value) = {
    campaignDao.updateStatus(campaignId, status)
      .map { res =>
        if (res == 0) throw InvalidCampaignStatusException(campaignId, status, "cannot update status")
        res
      }
  }
}
