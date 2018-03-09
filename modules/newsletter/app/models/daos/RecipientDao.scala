package models.daos

import java.util.UUID
import javax.inject.{Inject, Singleton}

import today.expresso.common.db.Repository
import today.expresso.common.exceptions._
import models.Recipient
import models.components.{CommonComponent, RecipientComponent}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import today.expresso.common.utils.SqlUtils

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class RecipientDao @Inject()(databaseConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with RecipientComponent with CommonComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  /**
    *
    * @param newsletterId
    * @return newsletter recipients with SUBSCRIBED status
    */
  def getByNewsletterId(userId: Long, newsletterId: Long, status: Option[Recipient.Status.Value] = None): Future[List[Recipient]] = {
    val query = sql"SELECT * FROM recipients_get_by_newsletter_id(${userId}, ${newsletterId}, ${status})".as[Recipient]
    db.run(query.asTry).map {
      SqlUtils.tryException {
        AuthorizationException.throwException
      }
    }.map(_.toList)
  }

  def add(userId: Long, newsletterId: Long, status: Option[Recipient.Status.Value] = None): Future[Recipient] = {
    val query = sql"SELECT * FROM recipients_add(${userId}, ${newsletterId}, ${status})".as[Recipient].head
    db.run(query.transactionally)
  }

  def verify(recipientId: UUID): Future[Recipient] = {
    val query = sql"SELECT * FROM recipients_update_status(${recipientId}, ${Recipient.Status.SUBSCRIBED})".as[Recipient].head
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException {
        RecipientNotFoundException.throwException
      }
    }
  }

  def unsubscribe(recipientId: UUID): Future[Recipient] = {
    val query = sql"SELECT * FROM recipients_update_status(${recipientId}, ${Recipient.Status.UNSUBSCRIBED})".as[Recipient].head
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException {
        RecipientNotFoundException.throwException
      }
    }
  }

  def remove(recipientId: UUID): Future[Recipient] = {
    val query = sql"SELECT * FROM recipients_update_status(${recipientId}, ${Recipient.Status.REMOVED})".as[Recipient].head
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException {
        RecipientNotFoundException.throwException
      }
    }
  }

  def clean(recipientId: UUID): Future[Recipient] = {
    val query = sql"SELECT * FROM recipients_update_status(${recipientId}, ${Recipient.Status.CLEANED})".as[Recipient].head
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException {
        RecipientNotFoundException.throwException
      }
    }
  }

  def spam(recipientId: UUID): Future[Recipient] = {
    val query = sql"SELECT * FROM recipients_update_status(${recipientId}, ${Recipient.Status.SPAM})".as[Recipient].head
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException {
        RecipientNotFoundException.throwException
      }
    }
  }
}
