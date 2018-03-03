package services

import java.net.URL

import config.TestContext
import exceptions.{AuthorizationException, NewsletterNotFoundException}
import play.api.i18n.Lang

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author im.
  */
class NewsletterServiceSpec extends TestContext {

  val newsletterService = app.injector.instanceOf[NewsletterService]

  val user = 1L

  "Newsletter service" must {

    "create newsletter" in {

      whenReady(newsletterService.create(user, "test", Lang("en"))) { newsletter =>
        newsletter.userId mustBe user
        newsletter.name mustBe "test"
        newsletter.locale mustBe Lang("en")
        newsletter.logoUrl mustBe None
        newsletter.avatarUrl mustBe None
        newsletter.options mustBe None
      }
    }

    "update newsletter" in {
      whenReady(
        newsletterService.create(user, "test", Lang("en"))
          .flatMap { newsletter =>
            newsletterService.update(
              user,
              newsletter.id,
              Some(Lang("ru")),
              Some(new URL("http://logo")),
              Some(new URL("http://avatar")),
              None)
          }
      ) { newsletter =>
        newsletter.userId mustBe user
        newsletter.name mustBe "test"
        newsletter.locale mustBe Lang("ru")
        newsletter.logoUrl mustBe Some(new URL("http://logo"))
        newsletter.avatarUrl mustBe Some(new URL("http://avatar"))
        newsletter.options mustBe None
      }
    }

    "get newsletter by id" in {
      whenReady(
        newsletterService.create(user, "test", Lang("en"))
          .flatMap { newsletter =>
            newsletterService.getById(user, newsletter.id)
          }
      ) { newsletter =>
        newsletter.userId mustBe user
        newsletter.name mustBe "test"
        newsletter.locale mustBe Lang("en")
        newsletter.logoUrl mustBe None
        newsletter.avatarUrl mustBe None
        newsletter.options mustBe None
      }
    }

    "get by user id" in {
      whenReady(
        newsletterService.create(user, "test", Lang("en"))
          .flatMap(_ => newsletterService.create(user, "test2", Lang("en")))
          .flatMap { _ =>
            newsletterService.getByUserId(user)
          }
      ) { newsletters =>
        newsletters.size mustBe 2
      }
    }

    "validate name and return false when duplicate" in {
      Await.result(newsletterService.create(user, "test", Lang("en")), 5.seconds)
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
      val newsletter = Await.result(newsletterService.create(user, "test", Lang("en")), 5.seconds)
      whenReady(newsletterService.update(2, newsletter.id, None, None, None, None).failed) { error =>
        error mustBe an [AuthorizationException]
      }
    }

    "don't allow to get newsletter if user is not owner or writer" in {
      val newsletter = Await.result(newsletterService.create(user, "test", Lang("en")), 5.seconds)
      whenReady(newsletterService.getById(2, newsletter.id).failed) { error =>
        error mustBe an [AuthorizationException]
      }
    }
  }
}
