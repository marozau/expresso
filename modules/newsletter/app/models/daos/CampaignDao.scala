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
import utils.{SqlUtils, Tx}

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

  def getByEditionId(userId: Long, editionId: Long): Future[Campaign] = {
    val query = sql"SELECT * FROM campaigns_get_by_edition_id(${userId}, ${editionId})".as[Campaign].head
    db.run(query.asTry).map {
      SqlUtils.tryException(CampaignNotFoundException.throwException)
    }
  }

  def createOrUpdate(userId: Long,
                     editionId: Long,
                     sendTime: Instant,
                     preview: Option[String],
                     options: Option[JsValue]): Future[Campaign] = {
    val query = sql"SELECT * FROM campaigns_create_or_update(${userId}, ${editionId}, ${sendTime}, ${preview}, ${options})".as[Campaign].head
    db.run(query.transactionally.withPinnedSession.asTry).map{
      SqlUtils.tryException(
        AuthorizationException.throwException,
        EditionNotFoundException.throwException,
        InvalidCampaignStatusException.throwException,
        InvalidCampaignScheduleException.throwException)
    }
  }

  def setPendingStatus(userId: Long, editionId: Long)(implicit tx: Tx[Campaign]): Future[Campaign] = {
    val query = sql"SELECT * FROM campaigns_set_status_pending(${userId}, ${editionId})".as[Campaign].head
      .flatMap { campaign =>
        DBIO.from(tx.tx(campaign)).map(_ => campaign)
      }
    db.run(query.transactionally.withPinnedSession.asTry).map{
      SqlUtils.tryException(
        AuthorizationException.throwException,
        CampaignNotFoundException.throwException,
        InvalidCampaignStatusException.throwException)
    }
  }

  def setSendingStatus(editionId: Long)(implicit tx: Tx[Campaign]) = {
    val query = sql"SELECT * FROM campaigns_set_status_sending(${editionId})".as[Campaign].head
      .flatMap { campaign =>
        DBIO.from(tx.tx(campaign)).map(_ => campaign)
      }
    db.run(query.transactionally.withPinnedSession.asTry).map{
      SqlUtils.tryException(
        AuthorizationException.throwException,
        CampaignNotFoundException.throwException,
        InvalidCampaignStatusException.throwException)
    }
  }

  def setSentStatus(editionId: Long)(implicit tx: Tx[Campaign]) = {
    val query = sql"SELECT * FROM campaigns_set_status_sent(${editionId})".as[Campaign].head
      .flatMap { campaign =>
        DBIO.from(tx.tx(campaign)).map(_ => campaign)
      }
    db.run(query.transactionally.withPinnedSession.asTry).map{
      SqlUtils.tryException(
        AuthorizationException.throwException,
        CampaignNotFoundException.throwException,
        InvalidCampaignStatusException.throwException)
    }
  }

  def suspend(userId: Long, editionId: Long)(implicit tx: Tx[Campaign]) ={
    val query = sql"SELECT * FROM campaigns_suspend(${userId}, ${editionId})".as[Campaign].head
      .flatMap { campaign =>
        DBIO.from(tx.tx(campaign)).map(_ => campaign)
      }
    db.run(query.transactionally.withPinnedSession.asTry).map{
      SqlUtils.tryException(
        AuthorizationException.throwException,
        CampaignNotFoundException.throwException,
        InvalidCampaignStatusException.throwException)
    }
  }

  def resume(userId: Long, editionId: Long)(implicit tx: Tx[Campaign]) ={
    val query = sql"SELECT * FROM campaigns_resume(${userId}, ${editionId})".as[Campaign].head
      .flatMap { campaign =>
        DBIO.from(tx.tx(campaign)).map(_ => campaign)
      }
    db.run(query.transactionally.withPinnedSession.asTry).map{
      SqlUtils.tryException(
        AuthorizationException.throwException,
        CampaignNotFoundException.throwException,
        InvalidCampaignStatusException.throwException)
    }
  }
}
