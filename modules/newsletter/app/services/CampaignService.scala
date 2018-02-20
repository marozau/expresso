package services

import java.time.Instant
import javax.inject.{Inject, Singleton}

import models.daos.CampaignDao
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class CampaignService @Inject()(campaignDao: CampaignDao)(implicit ec: ExecutionContext) {

  def getByEditionId(editionId: Long) = campaignDao.getByEditionId(editionId)

  def createOrUpdate(userId: Long, editionId: Long, sendTime: Instant, preview: Option[String], options: Option[JsValue]) = {
    campaignDao.createOrUpdate(userId, editionId, sendTime, preview, options) //TODO: event
  }

  def setPendingStatus(editionId: Long) = {
    campaignDao.setPendingStatus(editionId) //TODO: event
  }

  def setSendingStatus(editionId: Long) = {
    campaignDao.setSendingStatus(editionId) //TODO: event
  }

  def setSentStatus(editionId: Long) = {
    campaignDao.setSentStatus(editionId) //TODO: event
  }

  def setSuspendedStatus(editionId: Long) = {
    campaignDao.setSuspendedStatus(editionId) //TODO: event
  }
}
