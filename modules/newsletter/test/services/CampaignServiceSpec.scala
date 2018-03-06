package services

import java.time.{Instant, LocalDate}

import config.TestContext
import exceptions.{AuthorizationException, EditionNotFoundException, InvalidCampaignScheduleException, InvalidCampaignStatusException}
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

  var newsletter: Newsletter = _
  var edition: Edition = _

  val sendTime = Instant.now().plusSeconds(20.minutes.toSeconds)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    newsletter = Await.result(newsletterService.create(user, "test", Locale.ru), 5.seconds)
    edition = Await.result(editionService.create(user, newsletter.id, LocalDate.now()), 5.seconds)
  }

  "Campaign service" must {

    "create campaign" in {
      whenReady(campaignService.createOrUpdate(
        user,
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
        user,
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
        user,
        -1,
        sendTime,
        Some("preview"),
        None).failed
      ){ error =>
        error mustBe an [AuthorizationException]
      }
    }

    "don't update campaign if it is already scheduled" in {
      val campaign = Await.result(campaignService.createOrUpdate(user, edition.id, sendTime, None, None), 5.seconds)
      Await.result(campaignService.setPendingStatus(user, campaign.editionId), 1.second)
      whenReady(campaignService.createOrUpdate(
        user,
        edition.id,
        sendTime,
        Some("preview"),
        None).failed
      ){ error =>
        error mustBe an [InvalidCampaignStatusException]
      }
    }

    "change status" in {
      val campaign = Await.result(campaignService.createOrUpdate(user, edition.id, sendTime, None, None), 5.seconds)
      whenReady(campaignService.setPendingStatus(user, campaign.editionId)) { result =>
        result.status mustBe Campaign.Status.PENDING
      }

      whenReady(campaignService.setPendingStatus(user, campaign.editionId).failed) { error =>
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
      val campaign = Await.result(campaignService.createOrUpdate(user, edition.id, sendTime, None, None), 5.seconds)

      whenReady(campaignService.setSuspendedStatus(user, campaign.editionId).failed) { error =>
        error mustBe an [InvalidCampaignStatusException]
      }

      whenReady(campaignService.setPendingStatus(user, campaign.editionId)) { result =>
        result.status mustBe Campaign.Status.PENDING
      }

      whenReady(campaignService.setSuspendedStatus(user, campaign.editionId)) { result =>
        result.status mustBe Campaign.Status.SUSPENDED
      }

      whenReady(campaignService.setSuspendedStatus(user, campaign.editionId).failed) { error =>
        error mustBe an [InvalidCampaignStatusException]
      }
    }
  }

}
