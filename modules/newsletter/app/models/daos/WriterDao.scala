package models.daos

import javax.inject.{Inject, Singleton}
import today.expresso.common.db.Repository
import today.expresso.common.exceptions.AuthorizationException
import models.components.{EditionWriterComponent, NewsletterWriterComponent}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import today.expresso.common.utils.{SqlUtils, Tx}
import today.expresso.stream.domain.model.newsletter.{EditionWriter, NewsletterWriter}

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

  //TODO: test like event sourcing
  def addNewsletterWriter(userId: Long, newsletterId: Long, newUserId: Long)(implicit tx: Tx[NewsletterWriter]): Future[NewsletterWriter] = {
    val query = sql"SELECT * FROM newsletter_writers_add(${userId}, ${newsletterId}, ${newUserId})".as[NewsletterWriter].head
      .flatMap { writer =>
        DBIO.from(tx.tx(writer)).map(_ => writer)
      }
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException(
        AuthorizationException.throwException
      )
    }
  }

  def addEditionWriter(userId: Long, editionId: Long, newUserId: Long): Future[EditionWriter] = {
    val query = sql"SELECT * FROM edition_writers_add(${userId}, ${editionId}, ${newUserId})".as[EditionWriter].head
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException(
        AuthorizationException.throwException
      )
    }
  }
}
