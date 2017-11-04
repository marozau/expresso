package services

import javax.inject.{Inject, Singleton}

import models.daos.NewsletterDao

/**
  * @author im.
  */
@Singleton
class NewsletterService @Inject() (newsletterDao: NewsletterDao) {

  def list() = newsletterDao.list()
}
