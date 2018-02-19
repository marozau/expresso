package models.daos

import java.time.Instant
import javax.inject.{Inject, Singleton}

import db.Repository
import exceptions._
import models.Campaign
import models.components.CampaignComponent
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.JsValue
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import utils.SqlUtils

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class CampaignDao @Inject()(databaseConfigProvider: DatabaseConfigProvider,
                            newsletterDao: EditionDao,
                           )(implicit ec: ExecutionContext)
  extends Repository with CampaignComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._
  
  def createOrUpdate(editionId: Long, sendTime: Instant, preview: Option[String], options: Option[JsValue]): Future[Campaign] = {
    val query = sql"SELECT * FROM campaigns_create_or_update(${editionId}, ${sendTime}, ${preview}, ${options})".as[Campaign].head
    db.run(query.transactionally.asTry).map{
      SqlUtils.tryException(
        EditionNotFoundException.throwException,
        InvalidCampaignStatusException.throwException,
        InvalidCampaignScheduleException.throwException)
    }
  }

  def getByEditionId(editionId: Long): Future[Campaign] = {
    val query = sql"SELECT * FROM campaigns_get_by_edition_id(${editionId})".as[Campaign].head
    db.run(query.asTry).map {
      SqlUtils.tryException(CampaignNotFoundException.throwException)
    }
  }


  def setPendingStatus(editionId: Long): Future[Campaign] = {
    val query = sql"SELECT * FROM campaigns_set_status_pending(${editionId})".as[Campaign].head
    db.run(query.transactionally.asTry).map{
      SqlUtils.tryException(
        CampaignNotFoundException.throwException,
        InvalidCampaignStatusException.throwException)
    }
  }

  def setSendingStatus(editionId: Long) = {
    val query = sql"SELECT * FROM campaigns_set_status_sending(${editionId})".as[Campaign].head
    db.run(query.transactionally.asTry).map{
      SqlUtils.tryException(
        CampaignNotFoundException.throwException,
        InvalidCampaignStatusException.throwException)
    }
  }

  def setSentStatus(editionId: Long) ={
    val query = sql"SELECT * FROM campaigns_set_status_sent(${editionId})".as[Campaign].head
    db.run(query.transactionally.asTry).map{
      SqlUtils.tryException(
        CampaignNotFoundException.throwException,
        InvalidCampaignStatusException.throwException)
    }
  }

  def setSuspendedStatus(editionId: Long) ={
    val query = sql"SELECT * FROM campaigns_set_status_suspended(${editionId})".as[Campaign].head
    db.run(query.transactionally.asTry).map{
      SqlUtils.tryException(
        CampaignNotFoundException.throwException,
        InvalidCampaignStatusException.throwException)
    }
  }
}
