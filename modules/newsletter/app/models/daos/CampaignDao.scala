package models.daos

import java.time.Instant

import javax.inject.{Inject, Singleton}
import today.expresso.common.db.Repository
import today.expresso.common.exceptions._
import models.components.CampaignComponent
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.JsValue
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import today.expresso.common.utils.{SqlUtils, Tx}
import today.expresso.stream.domain.model.newsletter.Campaign

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
object CampaignDao {
  implicit val tx: Tx[Campaign] = c => Future.successful(c)
}

@Singleton
class CampaignDao @Inject()(databaseConfigProvider: DatabaseConfigProvider,
                            newsletterDao: EditionDao,
                           )(implicit ec: ExecutionContext)
  extends Repository with CampaignComponent {
  import CampaignDao._

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def getByEditionId(userId: Long, editionId: Long): Future[Campaign] = {
    val query = sql"SELECT * FROM campaigns_get_by_edition_id(${userId}, ${editionId})".as[Campaign].head
    db.run(query.withPinnedSession.asTry).map {
      SqlUtils.tryException(CampaignNotFoundException.throwException)
    }
  }

  def createOrUpdate(userId: Long,
                     editionId: Long,
                     sendTime: Instant,
                     preview: Option[String],
                     options: Option[JsValue])(implicit tx: Tx[Campaign]): Future[Campaign] = {
    val query = sql"SELECT * FROM campaigns_create_or_update(${userId}, ${editionId}, ${sendTime}, ${preview}, ${options})".as[Campaign].head
    db.run(query.transactionally.withPinnedSession.asTry).map {
      SqlUtils.tryException(
        AuthorizationException.throwException,
        EditionNotFoundException.throwException,
        InvalidCampaignStatusException.throwException,
        InvalidCampaignScheduleException.throwException)
    }
  }

  def start(userId: Long, editionId: Long)(implicit tx: Tx[Campaign]): Future[Campaign] = {
    val query = sql"SELECT * FROM campaigns_start(${userId}, ${editionId})".as[Campaign].head
      .flatMap { campaign =>
        DBIO.from(tx.tx(campaign)).map(_ => campaign)
      }
    db.run(query.transactionally.withPinnedSession.asTry).map {
      SqlUtils.tryException(
        AuthorizationException.throwException,
        CampaignNotFoundException.throwException,
        InvalidCampaignStatusException.throwException)
    }
  }

  def startSending(userId: Long, editionId: Long)(implicit tx: Tx[Campaign]) = {
    val query = sql"SELECT * FROM campaigns_start_sending(${userId}, ${editionId})".as[Campaign].head
      .flatMap { campaign =>
        DBIO.from(tx.tx(campaign)).map(_ => campaign)
      }
    db.run(query.transactionally.withPinnedSession.asTry).map {
      SqlUtils.tryException(
        AuthorizationException.throwException,
        CampaignNotFoundException.throwException,
        InvalidCampaignStatusException.throwException)
    }
  }

  def complete(userId: Long, editionId: Long, forced: Boolean)(implicit tx: Tx[Campaign]) = {
    val query = sql"SELECT * FROM campaigns_complete(${userId}, ${editionId}, ${forced})".as[Campaign].head
      .flatMap { campaign =>
        DBIO.from(tx.tx(campaign)).map(_ => campaign)
      }
    db.run(query.transactionally.withPinnedSession.asTry).map {
      SqlUtils.tryException(
        AuthorizationException.throwException,
        CampaignNotFoundException.throwException,
        InvalidCampaignStatusException.throwException)
    }
  }

  def suspend(userId: Long, editionId: Long)(implicit tx: Tx[Campaign]) = {
    val query = sql"SELECT * FROM campaigns_suspend(${userId}, ${editionId})".as[Campaign].head
      .flatMap { campaign =>
        DBIO.from(tx.tx(campaign)).map(_ => campaign)
      }
    db.run(query.transactionally.withPinnedSession.asTry).map {
      SqlUtils.tryException(
        AuthorizationException.throwException,
        CampaignNotFoundException.throwException,
        InvalidCampaignStatusException.throwException)
    }
  }

  def resume(userId: Long, editionId: Long)(implicit tx: Tx[Campaign]) = {
    val query = sql"SELECT * FROM campaigns_resume(${userId}, ${editionId})".as[Campaign].head
      .flatMap { campaign =>
        DBIO.from(tx.tx(campaign)).map(_ => campaign)
      }
    db.run(query.transactionally.withPinnedSession.asTry).map {
      SqlUtils.tryException(
        AuthorizationException.throwException,
        CampaignNotFoundException.throwException,
        InvalidCampaignStatusException.throwException)
    }
  }

  def suspendByUser(userId: Long, forced: Boolean)(implicit tx: Tx[Vector[Campaign]]) = {
    val query = sql"SELECT * FROM campaigns_suspend_by_user_id(${userId}, ${forced})".as[Campaign]
      .flatMap { campaigns =>
        DBIO.from(tx.tx(campaigns)).map(_ => campaigns)
      }
    db.run(query.transactionally.withPinnedSession.asTry).map {
      SqlUtils.tryException(
        AuthorizationException.throwException)
    }
  }
}
