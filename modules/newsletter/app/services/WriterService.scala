package services

import javax.inject.{Inject, Singleton}
import models.daos.WriterDao
import today.expresso.common.utils.Tx
import today.expresso.stream.domain.model.newsletter.NewsletterWriter

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class WriterService @Inject() (writerDao: WriterDao)(implicit ec: ExecutionContext) {

  implicit val tx: Tx[NewsletterWriter] = w => Future.successful(w)

  def addNewsletterWriter(userId: Long, newsletterId: Long, newUserId: Long) = {
    writerDao.addNewsletterWriter(userId, newsletterId, newUserId)
  }

  def addEditionWriter(userId: Long, editionId: Long, newUserId: Long) = {
    writerDao.addEditionWriter(userId, editionId, newUserId)
  }
}
