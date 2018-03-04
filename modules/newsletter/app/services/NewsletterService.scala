package services

import java.net.URL
import javax.inject.{Inject, Singleton}

import models.Locale
import models.daos.NewsletterDao
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class NewsletterService @Inject()(newsletterDao: NewsletterDao)(implicit ec: ExecutionContext) {

  def create(userId: Long, name: String, locale: Locale.Value) = {
    newsletterDao.create(userId, name, locale) //TODO: NewsletterCreated event
  }

  def update(userId: Long,
             newsletterId: Long,
             locale: Option[Locale.Value],
             logoUrl: Option[URL],
             avatarUrl: Option[URL],
             options: Option[JsValue]) = {
    newsletterDao.update(userId, newsletterId, locale, logoUrl.map(_.toString), avatarUrl.map(_.toString), options) //TODO: NewsletterUpdated event
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
