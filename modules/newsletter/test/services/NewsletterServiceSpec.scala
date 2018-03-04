package services

import java.net.URL

import config.TestContext
import exceptions.{AuthorizationException, NewsletterAlreadyExistException}
import models.Locale
import play.api.i18n.Lang

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * @author im.
  */
class NewsletterServiceSpec extends TestContext {
  import TestContext._

  val newsletterService = app.injector.instanceOf[NewsletterService]

  "Newsletter service" must {

    "create newsletter" in {

      whenReady(newsletterService.create(user, "test", Locale.en)) { newsletter =>
        newsletter.userId mustBe user
        newsletter.name mustBe "test"
        newsletter.locale mustBe Locale.en
        newsletter.logoUrl mustBe None
        newsletter.avatarUrl mustBe None
        newsletter.options mustBe None
      }
    }

    "don't create newsletter duplicate" in {
      Await.result(newsletterService.create(user, "test", Locale.en), 5.seconds)
      whenReady(newsletterService.create(user, "test", Locale.en).failed) { error =>
        error mustBe an[NewsletterAlreadyExistException]
      }
      whenReady(newsletterService.create(2, "test", Locale.en).failed) { error =>
        error mustBe an[NewsletterAlreadyExistException]
      }
      whenReady(newsletterService.create(2, "test", Locale.ru).failed) { error =>
        error mustBe an[NewsletterAlreadyExistException]
      }
    }

    "update newsletter" in {
      whenReady(
        newsletterService.create(user, "test", Locale.en)
          .flatMap { newsletter =>
            newsletterService.update(
              user,
              newsletter.id,
              Some(Locale.ru),
              Some(new URL("http://logo")),
              Some(new URL("http://avatar")),
              None)
          }
      ) { newsletter =>
        newsletter.userId mustBe user
        newsletter.name mustBe "test"
        newsletter.locale mustBe Locale.ru
        newsletter.logoUrl mustBe Some(new URL("http://logo"))
        newsletter.avatarUrl mustBe Some(new URL("http://avatar"))
        newsletter.options mustBe None
      }
    }

    "get newsletter by id" in {
      whenReady(
        newsletterService.create(user, "test", Locale.en)
          .flatMap { newsletter =>
            newsletterService.getById(user, newsletter.id)
          }
      ) { newsletter =>
        newsletter.userId mustBe user
        newsletter.name mustBe "test"
        newsletter.locale mustBe Locale.en
        newsletter.logoUrl mustBe None
        newsletter.avatarUrl mustBe None
        newsletter.options mustBe None
      }
    }

    "get by user id" in {
      whenReady(
        newsletterService.create(user, "test", Locale.en)
          .flatMap(_ => newsletterService.create(user, "test2", Locale.en))
          .flatMap { _ =>
            newsletterService.getByUserId(user)
          }
      ) { newsletters =>
        newsletters.size mustBe 2
      }
    }

    "validate name and return false when duplicate" in {
      Await.result(newsletterService.create(user, "test", Locale.en), 5.seconds)
      whenReady(
        newsletterService.validateName("test")
      ) {result =>
        result mustBe false
      }

      whenReady(
        newsletterService.validateName("test1")
      ) {result =>
        result mustBe true
      }
    }

    "don't allow to update newsletter if user is not owner" in {
      val newsletter = Await.result(newsletterService.create(user, "test", Locale.en), 5.seconds)
      whenReady(newsletterService.update(2, newsletter.id, None, None, None, None).failed) { error =>
        error mustBe an [AuthorizationException]
      }
    }

    "don't allow to get newsletter if user is not owner or writer" in {
      val newsletter = Await.result(newsletterService.create(user, "test", Locale.en), 5.seconds)
      whenReady(newsletterService.getById(2, newsletter.id).failed) { error =>
        error mustBe an [AuthorizationException]
      }
    }
  }
}