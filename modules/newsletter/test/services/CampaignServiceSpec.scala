package services

import java.time.{Instant, LocalDate}

import clients.Quartz
import config.TestContext
import exceptions.{AuthorizationException, InvalidCampaignScheduleException, InvalidCampaignStatusException}
import jobs.CampaignJob
import models.{Campaign, Edition, Locale, Newsletter}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author im.
  */
class CampaignServiceSpec extends TestContext {
  import TestContext._

  val campaignService = app.injector.instanceOf[CampaignService]
  val editionService = app.injector.instanceOf[EditionService]
  val newsletterService = app.injector.instanceOf[NewsletterService]
  val quartz = app.injector.instanceOf[Quartz]

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
      ){ error =>
        error mustBe an [InvalidCampaignScheduleException]
      }
    }

    "don't create campaign for unknown editions" in {
      whenReady(campaignService.createOrUpdate(
        userId,
        -1,
        sendTime,
        Some("preview"),
        None).failed
      ){ error =>
        error mustBe an [AuthorizationException]
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
      ){ error =>
        error mustBe an [InvalidCampaignStatusException]
      }
    }

    "change status" in {
      val campaign = Await.result(campaignService.createOrUpdate(userId, edition.id, sendTime, None, None), 5.seconds)
      whenReady(campaignService.setPendingStatus(userId, campaign.editionId)) { result =>
        result.status mustBe Campaign.Status.PENDING
      }

      whenReady(campaignService.setPendingStatus(userId, campaign.editionId).failed) { error =>
        error mustBe an [InvalidCampaignStatusException]
      }

      whenReady(campaignService.setSendingStatus(campaign.editionId)) { result =>
        result.status mustBe Campaign.Status.SENDING
      }

      whenReady(campaignService.setSendingStatus(campaign.editionId).failed) { error =>
        error mustBe an [InvalidCampaignStatusException]
      }

      whenReady(campaignService.setSentStatus(campaign.editionId)) { result =>
        result.status mustBe Campaign.Status.SENT
      }

      whenReady(campaignService.setSentStatus(campaign.editionId).failed) { error =>
        error mustBe an [InvalidCampaignStatusException]
      }
    }

    "set suspended status" in {
      val campaign = Await.result(campaignService.createOrUpdate(userId, edition.id, sendTime, None, None), 5.seconds)

      whenReady(campaignService.setSuspendedStatus(userId, campaign.editionId).failed) { error =>
        error mustBe an [InvalidCampaignStatusException]
      }

      whenReady(campaignService.setPendingStatus(userId, campaign.editionId)) { result =>
        result.status mustBe Campaign.Status.PENDING
      }

      whenReady(campaignService.setSuspendedStatus(userId, campaign.editionId)) { result =>
        result.status mustBe Campaign.Status.SUSPENDED_PENDING
      }

      whenReady(campaignService.setSuspendedStatus(userId, campaign.editionId).failed) { error =>
        error mustBe an [InvalidCampaignStatusException]
      }
    }

    "start campaign and schedule campaign job" in {
      val campaign = Await.result(campaignService.createOrUpdate(userId, edition.id, sendTime, None, None), 5.seconds)

      whenReady(campaignService.startCampaign(userId, campaign.editionId)) { campaign =>
        campaign.status mustBe Campaign.Status.PENDING
      }

      whenReady(campaignService.startCampaign(userId, campaign.editionId).failed) { error =>
        error mustBe an [InvalidCampaignStatusException]
      }

      whenReady(quartz.checkExists(CampaignJob.buildTrigger(userId, campaign).getKey)) { _ mustBe true}
    }

    "suspend started campaign and then resume it again" in {
      val campaign = Await.result(campaignService.createOrUpdate(userId, edition.id, sendTime, None, None), 5.seconds)
      Await.result(campaignService.startCampaign(userId, campaign.editionId), 5.seconds)

      whenReady(campaignService.suspendCampaign(userId, campaign.editionId)) { campaign =>
        campaign.status mustBe Campaign.Status.SUSPENDED_PENDING
      }

      whenReady(campaignService.suspendCampaign(userId, campaign.editionId).failed) { error =>
        error mustBe an [InvalidCampaignStatusException]
      }

      whenReady(quartz.getPausedTriggerGroups) { groups =>
        groups mustBe Set(s"campaign-${campaign.editionId}")
      }

      whenReady(campaignService.resumeCampaign(userId, campaign.editionId)) { campaign =>
        campaign.status mustBe Campaign.Status.PENDING
      }

      whenReady(campaignService.resumeCampaign(userId, campaign.editionId).failed) { error =>
        error mustBe an [InvalidCampaignStatusException]
      }

      whenReady(quartz.getPausedTriggerGroups) { groups =>
        groups.isEmpty mustBe true
      }
    }

    "suspend started edition sending and then resume it again" in {
      val campaign = Await.result(campaignService.createOrUpdate(userId, edition.id, sendTime, None, None), 5.seconds)
      Await.result(campaignService.startCampaign(userId, campaign.editionId), 5.seconds)

      val data = Map[String, AnyRef](
        "editionId" -> Predef.long2Long(campaign.editionId),
        "userId" -> Predef.long2Long(userId)
      )
      quartz.triggerJob(CampaignJob.buildJob(campaign).getKey, Quartz.newJobDataMap(data))

      whenReady(campaignService.getByEditionId(userId, campaign.editionId)) { campaign =>
        campaign.status mustBe Campaign.Status.SENDING
      }
    }
  }
}
