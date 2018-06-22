package services

import java.net.URL
import java.time.LocalDate

import config.TestContext
import today.expresso.common.exceptions.{AuthorizationException, NewsletterNotFoundException}
import play.api.libs.json.Json
import today.expresso.stream.domain.model.newsletter.{Locale, Newsletter}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author im.
  */
class EditionServiceSpec extends TestContext {

  import TestContext._

  val newsletterService = app.injector.instanceOf[NewsletterService]
  val editionService = app.injector.instanceOf[EditionService]

  var newsletter: Newsletter = _

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    newsletter = Await.result(newsletterService.create(userId, "test", Locale.ru), 5.seconds)
  }

  "Edition service" must {

    "create edition" in {
      whenReady(editionService.create(userId, newsletter.id, LocalDate.now())) { edition =>
        edition.newsletterId mustBe newsletter.id
        edition.date mustBe LocalDate.now()
        edition.url mustBe None
        edition.title mustBe None
        edition.header mustBe None
        edition.footer mustBe None
        edition.options mustBe None
      }
    }

    "update edition" in {
      val edition = Await.result(editionService.create(userId, newsletter.id, LocalDate.now()), 5.seconds)
      val header = Json.parse("""{"header":"hello"}""")
      val footer = Json.parse("""{"footer":"world"}""")
      whenReady(editionService.update(
        userId,
        edition.id,
        Some(LocalDate.now().plusDays(1)),
        Some(new URL("http://url")),
        Some("title"),
        Some(header),
        Some(footer),
        None)
      ) { result =>
        result.newsletterId mustBe newsletter.id
        result.date mustBe LocalDate.now().plusDays(1)
        result.url mustBe Some(new URL("http://url"))
        result.title mustBe  Some("title")
        result.header mustBe Some(header)
        result.footer mustBe Some(footer)
        result.options mustBe None
      }
      whenReady(editionService.update(
        userId,
        edition.id,
        None,
        None,
        None,
        None,
        None,
        None)
      ) { result =>
        result.newsletterId mustBe newsletter.id
        result.date mustBe LocalDate.now().plusDays(1)
        result.url mustBe Some(new URL("http://url"))
        result.title mustBe  Some("title")
        result.header mustBe Some(header)
        result.footer mustBe Some(footer)
        result.options mustBe None
      }
    }

    "don't allow to update edition if user is not owner or writer" in {
      val edition = Await.result(editionService.create(userId, newsletter.id, LocalDate.now()), 5.seconds)

      whenReady(
        editionService.update(2, edition.id, None, None, None, None, None, None).failed
      ) { error =>
        error mustBe an [AuthorizationException]
      }
    }

    "remove url" in {
      val edition = Await.result(editionService.create(userId, newsletter.id, LocalDate.now()), 5.seconds)
      Await.result(editionService.update(userId, edition.id, None, Some(new URL("http://url")), None, None, None, None), 5.seconds)

      whenReady(
        editionService.removeUrl(userId, edition.id)
      ) { result =>
        result.url mustBe None
      }
    }

    "don't remove for unknown edition" in {
      whenReady(
        editionService.removeUrl(userId, 1).failed
      ) { error =>
        error mustBe an [AuthorizationException]
      }
    }

    "get by id" in {
      val edition = Await.result(editionService.create(userId, newsletter.id, LocalDate.now()), 5.seconds)

      whenReady(
        editionService.getById(userId, edition.id)
      ) { result =>
        result.id mustBe edition.id
        result.newsletterId mustBe newsletter.id
      }
    }

    "throw exception when newsletter not found, in this system cannot detect newsletter owner and throws AuthorizationException" in {
      whenReady(
        editionService.getById(userId, 1).failed
      ) { error =>
        error mustBe an [AuthorizationException]
      }
    }

    "get by newsletter id" in {
      Await.result(editionService.create(userId, newsletter.id, LocalDate.now()), 5.seconds)
      Await.result(editionService.create(userId, newsletter.id, LocalDate.now().plusDays(1)), 5.seconds)

      whenReady(
        editionService.getByNewsletterId(userId, newsletter.id)
      ) { result =>
        result.size mustBe 2
      }
    }

    "don't return editions for unknown newsletter" in {
      whenReady(
        editionService.getByNewsletterId(userId, 3).failed
      ) { error =>
        error mustBe an [NewsletterNotFoundException]
      }
    }
  }
}
