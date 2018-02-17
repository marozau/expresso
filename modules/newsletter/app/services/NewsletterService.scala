package services

import java.net.URL
import javax.inject.{Inject, Singleton}

import models.daos.NewsletterDao
import play.api.i18n.Lang
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class NewsletterService @Inject()(newsletterDao: NewsletterDao)(implicit ec: ExecutionContext) {

  def create(userId: Long, name: String, locale: Lang) = {
    newsletterDao.create(userId, name, locale) //TODO: NewsletterCreated event
  }

  def update(userId: Long,
             newsletterId: Long,
             locale: Option[Lang],
             logoUrl: Option[URL],
             avatarUrl: Option[URL],
             options: Option[JsValue]) = {
    newsletterDao.update(userId, newsletterId, locale, logoUrl, avatarUrl, options) //TODO: NewsletterUpdated event
  }

  def getById(newsletterId: Long) = {
    newsletterDao.getById(newsletterId)
  }

  def getByUserId(userId: Long) = {
    newsletterDao.getByUserId(userId)
  }
}
