package models.daos

import javax.inject.{Inject, Singleton}

import models.{CampaignRecipient, CampaignRecipientStatistics}
import models.components.{CampaignRecipientComponent, CommonComponent}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import today.expresso.common.db.Repository
import today.expresso.common.exceptions.RecipientNotFoundException
import today.expresso.common.utils.SqlUtils

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class CampaignRecipientDao @Inject()(databaseConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with CampaignRecipientComponent with CommonComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def getStatistics(editionId: Long) = {
    val query = sql"SELECT * FROM campaign_recipients_statistics_get(${editionId})".as[CampaignRecipientStatistics].head
    db.run(query)
  }

  def getCampaignRecipient(userId: Long, editionId: Long) = {
    val query = sql"SELECT * FROM campaign_recipients_get(${userId}, ${editionId})".as[CampaignRecipientStatistics].head
    db.run(query)
  }

  def startSending(newsletterId: Long, editionId: Long) = {
    val query = sql"SELECT * FROM campaign_recipients_start_sending(${newsletterId}, ${editionId})".as[Unit].head
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException(RecipientNotFoundException.throwException)
    }
  }

  def markSent(userId: Long, editionId: Long) = {
    val query = sql"SELECT * FROM campaign_recipients_mark_sent(${userId}, ${editionId})".as[CampaignRecipient].head
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException(RecipientNotFoundException.throwException)
    }
  }

  def markFailed(userId: Long, editionId: Long, reason: Option[String]) = {
    val query = sql"SELECT * FROM campaign_recipients_mark_failed(${userId}, ${editionId}, ${reason})".as[CampaignRecipient].head
    db.run(query.transactionally.asTry).map {
      SqlUtils.tryException(RecipientNotFoundException.throwException)
    }
  }
}
