package services

import javax.inject.{Inject, Singleton}

import models.RecipientList
import models.daos.{RecipientDao, RecipientListDao}

import scala.concurrent.Future

/**
  * @author im.
  */
@Singleton
class RecipientService @Inject()(recipientListDao: RecipientListDao, recipientsDao: RecipientDao) {

  def getLists(userId: Long): Future[Seq[RecipientList]] = {
    recipientListDao.getByUserId(userId)
  }

  def getRecipients(userId: Long, listId: Long) = {
    recipientsDao.getByListId(userId, listId)
  }
}
