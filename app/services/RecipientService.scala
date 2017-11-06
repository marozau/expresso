package services

import javax.inject.{Inject, Singleton}

import models.daos.{RecipientDao, UserDao}

/**
  * @author im.
  */
@Singleton
class RecipientService @Inject()(recipientsDao: RecipientDao, userDao: UserDao) {

  def getNewsletterRecipients(newsletterId: Long) = {
    recipientsDao.getByNewsletterId(newsletterId)
  }

  def getEditionRecipients(editionId: Long) = recipientsDao.getByEditionId(editionId)

  def add(newsletterId: Long, userId: Long) = {
    recipientsDao.add(newsletterId, userId)
  }
}
