package services

import java.net.URL
import javax.inject.{Inject, Named, Singleton}

import models.Locale
import models.daos.NewsletterDao
import play.api.libs.json.JsValue
import streams.Names
import today.expresso.stream.Producer

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class NewsletterService @Inject()(newsletterDao: NewsletterDao)
                                 (implicit ec: ExecutionContext, @Named(Names.newsletter) producer: Producer) {

  import models.Newsletter._

  def create(userId: Long, name: String, locale: Locale.Value) = Producer.transactionally {
    newsletterDao.create(userId, name, locale) { newsletter =>
      producer.send(ToNewsletterCreated(newsletter))
    }
  }

  def update(userId: Long,
             newsletterId: Long,
             locale: Option[Locale.Value],
             logoUrl: Option[URL],
             avatarUrl: Option[URL],
             options: Option[JsValue]) = Producer.transactionally {
    newsletterDao.update(userId, newsletterId, locale, logoUrl.map(_.toString), avatarUrl.map(_.toString), options) { newsletter =>
      producer.send(ToNewsletterUpdated(newsletter))
    }
  }

  def getById(userId: Long, newsletterId: Long) = Producer.transactionally {
    newsletterDao.getById(userId, newsletterId)
  }

  def getByUserId(userId: Long) = {
    newsletterDao.getByUserId(userId)
  }

  def validateName(name: String) = {
    newsletterDao.validateName(name)
  }
}
