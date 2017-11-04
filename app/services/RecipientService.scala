package services

import javax.inject.{Inject, Singleton}

import models.Recipient
import models.daos.RecipientDao

import scala.concurrent.Future

/**
  * @author im.
  */
@Singleton
class RecipientService @Inject()(recipientsDao: RecipientDao) {

  def getNewsletterRecipients(newsletterId: Long): Future[Seq[Recipient]] = {
    recipientsDao.getByNewsletterId(newsletterId)
  }

  def getEditionRecipients(editionId: Long) = recipientsDao.getByEditionId(editionId)
}
