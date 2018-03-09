package services

import java.time.{Instant, LocalDate}

import clients.Quartz
import config.TestContext
import exceptions.{AuthorizationException, InvalidCampaignScheduleException, InvalidCampaignStatusException}
import jobs.campaign.{CampaignJob, PendingJob}
import models.daos.RecipientDao
import models._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.runtime.BoxedUnit

/**
  * @author im.
  */
class CampaignServiceSpec extends TestContext {

  import TestContext._

  val campaignService = app.injector.instanceOf[CampaignService]
  val editionService = app.injector.instanceOf[EditionService]
  val newsletterService = app.injector.instanceOf[NewsletterService]
  val quartz = app.injector.instanceOf[Quartz]
  val recipientDao = app.injector.instanceOf[RecipientDao]

  var newsletter: Newsletter = _
  var edition: Edition = _

  val sendTime = Instant.now().plusSeconds(20.minutes.toSeconds)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    newsletter = Await.result(newsletterService.create(userId, "test", Locale.ru), 5.seconds)
    edition = Await.result(editionService.create(userId, newsletter.id, LocalDate.now()), 5.seconds)
  }

  "Campaign service" must {

    "create campaign" in {
      whenReady(campaignService.createOrUpdate(
        userId,
        edition.id,
        sendTime,
        Some("preview"),
        None
      )
      ) { campaign =>
        campaign.editionId mustBe edition.id
        campaign.newsletterId mustBe newsletter.id
        campaign.sendTime mustBe sendTime
        campaign.status mustBe Campaign.Status.NEW
        campaign.preview mustBe Some("preview")
      }
    }

    "don't create campaign in less than 15 minutes before start" in {
      whenReady(campaignService.createOrUpdate(
        userId,
        edition.id,
        Instant.now(),
        Some("preview"),
        None).failed
      ) { error =>
        error mustBe an[InvalidCampaignScheduleException]
      }
    }

    "don't create campaign for unknown editions" in {
      whenReady(campaignService.createOrUpdate(
        userId,
        -1,
        sendTime,
        Some("preview"),
        None).failed
      ) { error =>
        error mustBe an[AuthorizationException]
      }
    }

    "don't update campaign if it is already scheduled" in {
      val campaign = Await.result(campaignService.createOrUpdate(userId, edition.id, sendTime, None, None), 5.seconds)
      Await.result(campaignService.setPendingStatus(userId, campaign.editionId), 1.second)
      whenReady(campaignService.createOrUpdate(
        userId,
        edition.id,
        sendTime,
        Some("preview"),
        None).failed
      ) { error =>
        error mustBe an[InvalidCampaignStatusException]
      }
    }

    "start campaign and schedule campaign job" in {
      val campaign = Await.result(campaignService.createOrUpdate(userId, edition.id, sendTime, None, None), 5.seconds)

      whenReady(campaignService.startCampaign(userId, campaign.editionId)) { campaign =>
        campaign.status mustBe Campaign.Status.PENDING
      }

      whenReady(campaignService.startCampaign(userId, campaign.editionId).failed) { error =>
        error mustBe an[InvalidCampaignStatusException]
      }

      whenReady(quartz.checkExists(PendingJob.buildTrigger(userId, campaign).getKey)) {
        _ mustBe true
      }
    }

    "suspend started campaign and then resume it again" in {
      val campaign = Await.result(campaignService.createOrUpdate(userId, edition.id, sendTime, None, None), 5.seconds)
      Await.result(campaignService.startCampaign(userId, campaign.editionId), 5.seconds)

      whenReady(campaignService.suspendCampaign(userId, campaign.editionId)) { campaign =>
        campaign.status mustBe Campaign.Status.SUSPENDED_PENDING
      }

      whenReady(campaignService.suspendCampaign(userId, campaign.editionId).failed) { error =>
        error mustBe an[InvalidCampaignStatusException]
      }

      whenReady(quartz.getPausedTriggerGroups) { groups =>
        groups mustBe Set(CampaignJob.identity(campaign))
      }

      whenReady(campaignService.resumeCampaign(userId, campaign.editionId)) { campaign =>
        campaign.status mustBe Campaign.Status.PENDING
      }

      whenReady(campaignService.resumeCampaign(userId, campaign.editionId).failed) { error =>
        error mustBe an[InvalidCampaignStatusException]
      }

      whenReady(quartz.getPausedTriggerGroups) { groups =>
        groups.isEmpty mustBe true
      }
    }

    "start edition sending then suspend it and then resume it again" in {
      val recipient = Await.result(recipientDao.add(userId, newsletter.id, Some(Recipient.Status.SUBSCRIBED)), 1.seconds)
      val campaign = Await.result(campaignService.createOrUpdate(userId, edition.id, sendTime, None, None), 1.seconds)
      whenReady(campaignService.startCampaign(userId, campaign.editionId)) { _ => Unit }

      whenReady(campaignService.startSending(userId, campaign.editionId)) { campaign =>
        campaign.status mustBe Campaign.Status.SENDING
      }

      whenReady(campaignService.suspendCampaign(userId, campaign.editionId)) { campaign =>
        campaign.status mustBe Campaign.Status.SUSPENDED_SENDING
      }

      whenReady(campaignService.suspendCampaign(userId, campaign.editionId).failed) { error =>
        error mustBe an[InvalidCampaignStatusException]
      }

      whenReady(quartz.getPausedTriggerGroups) { groups =>
        groups mustBe Set(CampaignJob.identity(campaign))
      }

      whenReady(campaignService.resumeCampaign(userId, campaign.editionId)) { campaign =>
        campaign.status mustBe Campaign.Status.SENDING
      }

      whenReady(campaignService.resumeCampaign(userId, campaign.editionId).failed) { error =>
        error mustBe an[InvalidCampaignStatusException]
      }

      whenReady(quartz.getPausedTriggerGroups) { groups =>
        groups.isEmpty mustBe true
      }
    }

    "start campaign sending job" in {
      val recipient = Await.result(recipientDao.add(userId, newsletter.id, Some(Recipient.Status.SUBSCRIBED)), 1.seconds)
      val campaign = Await.result(campaignService.createOrUpdate(userId, edition.id, sendTime, None, None), 1.seconds)
      whenReady(campaignService.startCampaign(userId, campaign.editionId)){ _ => Unit}

      whenReady(quartz.triggerJob(PendingJob.buildJob(campaign).getKey, PendingJob.buildJobData(userId, campaign))) { result =>
        result mustBe an [BoxedUnit]
      }

      whenReady(campaignService.getByEditionId(userId, campaign.editionId)) { campaign =>
        campaign.status mustBe Campaign.Status.SENDING
      }
    }
  }
}
