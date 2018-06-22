package services

import java.lang.invoke.MethodHandles
import java.time.Instant

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import models.daos.CampaignDao
import org.slf4j.LoggerFactory
import play.api.libs.json.JsValue
import services.utils.CampaignScheduler
import today.expresso.stream.domain.event.newsletter.{CampaignStarted, CampaignUpdated}
import today.expresso.stream.domain.model.newsletter.Campaign

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class CampaignService @Inject()(campaignDao: CampaignDao, campaignScheduler: CampaignScheduler)
                               (implicit ec: ExecutionContext, system: ActorSystem) {

  import Campaign._

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  //TODO: test stream.subscribe(self, classOf[Domain]). We can subscribe on classOf[External] and publish those to kafka
  val stream = system.eventStream

  def getByEditionId(userId: Long, editionId: Long) = campaignDao.getByEditionId(userId, editionId)

  def createOrUpdate(userId: Long, editionId: Long, sendTime: Instant, preview: Option[String], options: Option[JsValue]) = {
      logger.info(s"createOrUpdate, userId=$userId, editionId=$editionId")
      campaignDao.createOrUpdate(userId, editionId, sendTime, preview, options) { campaign =>
        Future.successful(stream.publish(CampaignUpdated(campaign)))

    }
  }

  def startCampaign(userId: Long, editionId: Long) = {
    logger.info(s"startCampaign, userId=$userId, editionId=$editionId")
    campaignDao.start(userId, editionId) { campaign =>
      campaignScheduler.schedule(userId, campaign).map { _ =>
        stream.publish(CampaignUpdated(campaign))
        stream.publish(CampaignStarted(campaign))
      }
    }
  }

  def startSendingCampaign(userId: Long, editionId: Long) = {
    logger.info(s"startSendingCampaign, userId=$userId, editionId=$editionId")
    campaignDao.startSending(userId, editionId) { campaign =>
      campaignScheduler.startSending(userId, campaign)
        .map(_ => stream.publish(CampaignUpdated(campaign)))
    }
  }

  def suspendCampaign(userId: Long, editionId: Long) = {
    logger.info(s"suspendCampaign, userId=$userId, editionId=$editionId")
    campaignDao.suspend(userId, editionId) { campaign =>
      campaignScheduler.suspend(campaign)
        .map(_ => stream.publish(CampaignUpdated(campaign)))
    }
  }

  def resumeCampaign(userId: Long, editionId: Long) = {
    logger.info(s"resumeCampaign, userId=$userId, editionId=$editionId")
    campaignDao.resume(userId, editionId) { campaign =>
      campaignScheduler.resume(campaign)
        .map(_ => stream.publish(CampaignUpdated(campaign)))
    }
  }

  def stopCampaign(userId: Long, editionId: Long) = {
    logger.info(s"stopCampaign, userId=$userId, editionId=$editionId")
  }

  def completeCampaign(userId: Long, editionId: Long, forced: Boolean) = {
    logger.info(s"completeCampaign, userId=$userId, editionId=$editionId, forced=$forced")
    campaignDao.complete(userId, editionId, forced) { campaign =>
      Future.successful(stream.publish(CampaignUpdated(campaign)))
    }
  }

  def suspendUserCampaigns(userId: Long, forced: Boolean) = {
    logger.info(s"suspendUserCampaigns, userId=$userId")
    campaignDao.suspendByUser(userId, forced) { campaigns =>
      campaignScheduler.suspendByUser(userId)
        .map(_ => campaigns.foreach(c => stream.publish(CampaignUpdated(c))))
    }
  }
}
