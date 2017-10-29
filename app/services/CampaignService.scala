package services

import javax.inject.{Inject, Singleton}

import models.Campaign
import models.daos.CampaignDao

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class CampaignService @Inject() (campaignDao: CampaignDao)(implicit ec: ExecutionContext) {

}
