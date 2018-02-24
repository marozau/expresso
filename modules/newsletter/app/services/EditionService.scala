package services

import java.net.URL
import java.time.LocalDate
import javax.inject.{Inject, Singleton}

import models.daos.EditionDao
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class EditionService @Inject()(editionDao: EditionDao)(implicit ec: ExecutionContext) {

  def create(userId: Long, newsletterId: Long, date: LocalDate) = {
    editionDao.create(userId, newsletterId, date) //TODO: NewsletterCreated event
  }

  //TODO: EditionUpdate command
  //TODO: EditionUpdateDto(proto-gRPC)(command) -> EditionUpdate(scala) -> EditionUpdated(avro)(eventg[)
  def update(userId: Long,
             editionId: Long,
             date: Option[LocalDate],
             url: Option[URL] = None,
             title: Option[String],
             header: Option[JsValue],
             footer: Option[JsValue],
             options: Option[JsValue]) = {
    editionDao.update(userId, editionId, date, url.map(_.toString), title, header, footer, options) //TODO: NewsletterUpdated event
  }

  def getById(userId: Long, id: Long) = editionDao.getById(userId, id)

  def getByNewsletterId(userId: Long, newsletterId: Long) = editionDao.getByNewsletterId(userId, newsletterId)


}