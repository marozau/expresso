package services

import java.time.{Instant, LocalDate}

import config.TestContext
import models._
import today.expresso.stream.domain.model.newsletter._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.runtime.BoxedUnit

/**
  * @author im.
  */
class CampaignRecipientServiceSpec extends TestContext {

  import TestContext._

  val campaignRecipientService = app.injector.instanceOf[CampaignRecipientService]
  val editionService = app.injector.instanceOf[EditionService]
  val newsletterService = app.injector.instanceOf[NewsletterService]
  val campaignService = app.injector.instanceOf[CampaignService]
  val recipientService = app.injector.instanceOf[RecipientService]

  var newsletter: Newsletter = _
  var edition: Edition = _
  var campaign: Campaign = _
  var recipient: Recipient = _

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    newsletter = Await.result(newsletterService.create(userId, "test", Locale.ru), 5.seconds)
    edition = Await.result(editionService.create(userId, newsletter.id, LocalDate.now()), 5.seconds)
    campaign = Await.result(campaignService.createOrUpdate(userId, edition.id, Instant.now().plusSeconds(20.minutes.toSeconds), Some("preview"), None), 5.seconds)
    recipient = Await.result(recipientService.subscribeUser(userId, newsletter.id), 5.seconds)
  }

  "Campaign Recipient Service" must {

    "get statistics for unknown edition" in {
      whenReady(campaignRecipientService.getStatistics(edition.id)) { statistics =>
        statistics.editionId mustBe edition.id
        statistics.count mustBe 0
        statistics.sending mustBe 0
        statistics.sent mustBe 0
        statistics.failed mustBe 0
      }
    }

    "campaign recipient life cycle" in {

      whenReady(campaignRecipientService.startSending(newsletter.id, edition.id)) { res =>
        res mustBe an[BoxedUnit]
      }

      whenReady(campaignRecipientService.getStatistics(edition.id)) { statistics =>
        statistics.editionId mustBe edition.id
        statistics.count mustBe 1
        statistics.sending mustBe 1
        statistics.sent mustBe 0
        statistics.failed mustBe 0
      }

      whenReady(campaignRecipientService.markSent(userId, edition.id)) { campaignRecipient =>
        campaignRecipient.recipientId mustBe recipient.id
        campaignRecipient.editionId mustBe edition.id
        campaignRecipient.status mustBe Campaign.Status.SENT
        campaignRecipient.attempts mustBe 0
        campaignRecipient.reason mustBe None
      }

      whenReady(campaignRecipientService.getStatistics(edition.id)) { statistics =>
        statistics.editionId mustBe edition.id
        statistics.count mustBe 1
        statistics.sending mustBe 0
        statistics.sent mustBe 1
        statistics.failed mustBe 0
      }
    }

    "mark as failed" in {
      whenReady(campaignRecipientService.startSending(newsletter.id, edition.id)) { res =>
        res mustBe an[BoxedUnit]
      }
      whenReady(campaignRecipientService.markFailed(userId, edition.id, Some("reason"))) { campaignRecipient =>
        campaignRecipient.recipientId mustBe recipient.id
        campaignRecipient.editionId mustBe edition.id
        campaignRecipient.status mustBe Campaign.Status.SENDING
        campaignRecipient.attempts mustBe 1
        campaignRecipient.reason mustBe Some("reason")
      }

    }
  }
}
