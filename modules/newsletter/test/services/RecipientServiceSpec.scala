package services

import config.TestContext
import models.daos.RecipientDao
import models.{Locale, Newsletter, Recipient}
import today.expresso.grpc.user.domain.User

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * @author im.
  */
class RecipientServiceSpec extends TestContext {

  import TestContext._

  val recipientService = app.injector.instanceOf[RecipientService]
  val recipientDao = app.injector.instanceOf[RecipientDao]

  val campaignService = app.injector.instanceOf[CampaignService]
  val newsletterService = app.injector.instanceOf[NewsletterService]

  var newsletter: Newsletter = _

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    newsletter = Await.result(newsletterService.create(userId, "test", Locale.ru), 5.seconds)
  }


  "Recipient service" must {

    "subscribe user" in {
      whenReady(recipientService.subscribeUser(userId, newsletter.id)) { recipient =>
        recipient.userId mustBe userId
        recipient.newsletterId mustBe newsletter.id
        recipient.status mustBe Recipient.Status.SUBSCRIBED
      }
    }

    "subscribe email" in {
      import org.mockito.Mockito._
      import org.mockito.ArgumentMatchers._
      when(mockUserService.createReader(userEmail)).thenReturn(Future.successful(user))
      when(mockMailService.sendVerification(anyString, any(classOf[User]), any(classOf[Recipient]))).thenReturn(Future.successful(()))

      whenReady(recipientService.subscribeEmail(userEmail, newsletter.id)) { recipient =>
        recipient.userId mustBe userId
        recipient.newsletterId mustBe newsletter.id
        recipient.status mustBe Recipient.Status.PENDING

        verify(mockMailService).sendVerification(userEmail, user, recipient)
      }
    }

    "verify subscription" in {
      val recipient = Await.result(recipientDao.add(userId, newsletter.id, Some(Recipient.Status.PENDING)), 1.seconds)
      recipient.status mustBe Recipient.Status.PENDING

      whenReady(recipientService.verify(recipient.id)) { recipient =>
        recipient.userId mustBe userId
        recipient.newsletterId mustBe newsletter.id
        recipient.status mustBe Recipient.Status.SUBSCRIBED
      }
    }

    "unsubscribe recipient" in {
      val recipient = Await.result(recipientDao.add(userId, newsletter.id, Some(Recipient.Status.SUBSCRIBED)), 1.seconds)
      recipient.status mustBe Recipient.Status.SUBSCRIBED

      whenReady(recipientService.unsubscribe(recipient.id)) { recipient =>
        recipient.userId mustBe userId
        recipient.newsletterId mustBe newsletter.id
        recipient.status mustBe Recipient.Status.UNSUBSCRIBED
      }
    }

    "clean recipient" in {
      val recipient = Await.result(recipientDao.add(userId, newsletter.id, Some(Recipient.Status.SUBSCRIBED)), 1.seconds)
      recipient.status mustBe Recipient.Status.SUBSCRIBED

      whenReady(recipientService.clean(recipient.id)) { recipient =>
        recipient.userId mustBe userId
        recipient.newsletterId mustBe newsletter.id
        recipient.status mustBe Recipient.Status.CLEANED
      }
    }

    "remove recipient" in {
      val recipient = Await.result(recipientDao.add(userId, newsletter.id, Some(Recipient.Status.SUBSCRIBED)), 1.seconds)
      recipient.status mustBe Recipient.Status.SUBSCRIBED

      whenReady(recipientService.remove(recipient.id)) { recipient =>
        recipient.userId mustBe userId
        recipient.newsletterId mustBe newsletter.id
        recipient.status mustBe Recipient.Status.REMOVED
      }
    }

    "spam recipient" in {
      val recipient = Await.result(recipientDao.add(userId, newsletter.id, Some(Recipient.Status.SUBSCRIBED)), 1.seconds)
      recipient.status mustBe Recipient.Status.SUBSCRIBED

      whenReady(recipientService.spam(recipient.id)) { recipient =>
        recipient.userId mustBe userId
        recipient.newsletterId mustBe newsletter.id
        recipient.status mustBe Recipient.Status.SPAM
      }
    }

    "get by newsletter id" in {
      Await.result(recipientDao.add(1L, newsletter.id, Some(Recipient.Status.PENDING)), 1.seconds)
      Await.result(recipientDao.add(2L, newsletter.id, Some(Recipient.Status.SUBSCRIBED)), 1.seconds)
      Await.result(recipientDao.add(3L, newsletter.id, Some(Recipient.Status.SUBSCRIBED)), 1.seconds)
      Await.result(recipientDao.add(4L, newsletter.id, Some(Recipient.Status.SUBSCRIBED)), 1.seconds)
      Await.result(recipientDao.add(5L, newsletter.id, Some(Recipient.Status.SPAM)), 1.seconds)

      whenReady(recipientService.getByNewsletterId(userId, newsletter.id, None)) { result =>
        result.size mustBe 5
      }

      whenReady(recipientService.getByNewsletterId(userId, newsletter.id, Some(Recipient.Status.SUBSCRIBED))) { result =>
        result.size mustBe 3
      }
    }
  }
}
