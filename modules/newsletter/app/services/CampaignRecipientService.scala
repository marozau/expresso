package services

import java.lang.invoke.MethodHandles
import javax.inject.{Inject, Singleton}

import models.daos.CampaignRecipientDao
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class CampaignRecipientService @Inject()(campaignRecipientDao: CampaignRecipientDao)(implicit ec: ExecutionContext) {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  def getStatistics(editionId: Long) = {
    campaignRecipientDao.getStatistics(editionId)
  }

  def getCampaignRecipient(userId: Long, editionId: Long) = {
    campaignRecipientDao.getCampaignRecipient(userId, editionId)
  }

  def startSending(newsletterId: Long, editionId: Long) = {
    logger.info(s"startSending, newsletterId=${newsletterId}, editionId=${editionId}")
    campaignRecipientDao.startSending(newsletterId, editionId)
  }

  def markSent(userId: Long, editionId: Long) = {
    logger.info(s"markSent, userId=${userId}, editionId=${editionId}")
    campaignRecipientDao.markSent(userId, editionId) //TODO: event
  }

  def markFailed(userId: Long, editionId: Long, reason: Option[String]) = {
    logger.info(s"markFailed, userId=${userId}, editionId=${editionId}, reason=${reason}")
    campaignRecipientDao.markFailed(userId, editionId, reason) //TODO: event
  }
}
