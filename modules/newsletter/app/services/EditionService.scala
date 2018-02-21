package services

import java.net.URL
import java.util.Date
import javax.inject.{Inject, Singleton}

import models.daos.{CampaignDao, EditionDao}
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class EditionService @Inject()(editionDao: EditionDao)(implicit ec: ExecutionContext) {

  def create(userId: Long, newsletterId: Long, date: Date) = {
    editionDao.create(userId, newsletterId, date) //TODO: NewsletterCreated event
  }

  //TODO: EditionUpdate command
  //TODO: EditionUpdateDto(proto-gRPC)(command) -> EditionUpdate(scala) -> EditionUpdated(avro)(eventg[)
  def update(userId: Long,
             editionId: Long,
             date: Option[Date],
             url: Option[URL] = None,
             title: Option[String],
             header: Option[JsValue],
             footer: Option[JsValue],
             options: Option[JsValue]) = {
    editionDao.update(userId, editionId, date, url, title, header, footer, options) //TODO: NewsletterUpdated event
  }

  def getById(userId: Long, id: Long) = editionDao.getById(userId, id)

  def getByNewsletterId(userId: Long, newsletterId: Long) = editionDao.getByNewsletterId(userId, newsletterId)


}