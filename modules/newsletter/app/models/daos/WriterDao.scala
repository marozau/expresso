package models.daos

import javax.inject.{Inject, Singleton}

import db.Repository
import exceptions.AuthorizationException
import models.{EditionWriter, NewsletterWriter}
import models.components.{EditionWriterComponent, NewsletterWriterComponent}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import utils.SqlUtils

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class WriterDao @Inject()(databaseConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with NewsletterWriterComponent with EditionWriterComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._


  def addNewsletterWriter(userId: Long, newsletterId: Long, newUserId: Long): Future[NewsletterWriter] = {
    val query = sql"SELECT * FROM newsletter_writers_add(${userId}, ${newsletterId}, ${newUserId})".as[NewsletterWriter].head
    db.run(query.transactionally.asTry).map{
      SqlUtils.tryException(
        AuthorizationException.throwException
      )
    }
  }

  def addEditionWriter(userId: Long, editionId: Long, newUserId: Long): Future[EditionWriter] = {
    val query = sql"SELECT * FROM edition_writers_add(${userId}, ${editionId}, ${newUserId})".as[EditionWriter].head
    db.run(query.transactionally.asTry).map{
      SqlUtils.tryException(
        AuthorizationException.throwException
      )
    }
  }
}
