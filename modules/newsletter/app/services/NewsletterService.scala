package services

import java.net.URL

import javax.inject.{Inject, Singleton}
import models.Locale
import models.daos.NewsletterDao
import play.api.libs.json.JsValue
import today.expresso.stream.ProducerPool

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class NewsletterService @Inject()(newsletterDao: NewsletterDao, pp: ProducerPool)
                                 (implicit ec: ExecutionContext) {

  import models.Newsletter._

  def create(userId: Long, name: String, locale: Locale.Value) = pp.transaction { producer =>
    newsletterDao.create(userId, name, locale) { newsletter =>
      producer.send(ToNewsletterCreated(newsletter))
    }
  }

  def update(userId: Long,
             newsletterId: Long,
             locale: Option[Locale.Value],
             logoUrl: Option[URL],
             avatarUrl: Option[URL],
             options: Option[JsValue]) =  pp.transaction { producer =>
    newsletterDao.update(userId, newsletterId, locale, logoUrl.map(_.toString), avatarUrl.map(_.toString), options) { newsletter =>
      producer.send(ToNewsletterUpdated(newsletter))
    }
  }

  def getById(userId: Long, newsletterId: Long) = {
    newsletterDao.getById(userId, newsletterId)
  }

  def getByUserId(userId: Long) = {
    newsletterDao.getByUserId(userId)
  }

  def validateName(name: String) = {
    newsletterDao.validateName(name)
  }
}
