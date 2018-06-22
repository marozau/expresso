package services

import java.net.URL

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import models.daos.NewsletterDao
import play.api.libs.json.JsValue
import today.expresso.stream.domain.event.newsletter.{NewsletterCreated, NewsletterUpdated}
import today.expresso.stream.domain.model.newsletter.Locale

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class NewsletterService @Inject()(newsletterDao: NewsletterDao)
                                 (implicit ec: ExecutionContext, system: ActorSystem) {

  val stream = system.eventStream

  def create(userId: Long, name: String, locale: Locale.Value) = {
    newsletterDao.create(userId, name, locale) { newsletter =>
      Future.successful(stream.publish(NewsletterCreated(newsletter)))
    }
  }

  def update(userId: Long,
             newsletterId: Long,
             locale: Option[Locale.Value],
             logoUrl: Option[URL],
             avatarUrl: Option[URL],
             options: Option[JsValue]) =  {
    newsletterDao.update(userId, newsletterId, locale, logoUrl.map(_.toString), avatarUrl.map(_.toString), options) { newsletter =>
      Future.successful(stream.publish(NewsletterUpdated(newsletter)))
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
