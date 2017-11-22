package models.daos

import javax.inject.{Inject, Singleton}

import exceptions.{InvalidUserStatusException, UserNotFoundException}
import models.api.Repository
import models.components.{EditionComponent, NewsletterComponent, RecipientComponent, UserComponent}
import models.{Recipient, UserStatus}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class RecipientDao @Inject()(databaseConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with RecipientComponent with UserComponent with EditionComponent with NewsletterComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  implicit def recipientCast(r: DBRecipient) = Recipient(r.id, r.newsletterId, r.userId, r.status)

  /**
    *
    * @param newsletterId
    * @return newsletter recipients with SUBSCRIBED status
    */
  def getByNewsletterId(newsletterId: Long) = db.run {
    recipients.filter(r => r.newsletterId === newsletterId && r.status === Recipient.Status.SUBSCRIBED).result
      .map(_.map(recipientCast))
  }

  /**
    *
    * @param newsletterId
    * @return all newsletter recipients despite of recipient status
    */
  def getAllByNewsletterId(newsletterId: Long) = db.run {
    recipients.filter(_.newsletterId === newsletterId).result
      .map(_.map(recipientCast))
  }

  def getByEditionId(editionId: Long) = {
    val recipientsQuery = for {
      edition <- editions.filter(_.id === editionId)
      newsletter <- newsletters.filter(_.id === edition.newsletterId)
      recipients <- recipients.filter(_.newsletterId === newsletter.id)
    } yield recipients

    db.run(recipientsQuery.result).map(_.map(recipientCast))
  }

  //TODO: process case of unsubscribed user who wants to subsribe again
  def add(newsletterId: Long, userId: Long, status: Recipient.Status.Value) = {
    val recipientAdd = for {
      userOption <- users.filter(_.id === userId).result.headOption
      status <- if (userOption.isDefined) DBIO.successful(getStatus(userOption.get.status, status))
      else DBIO.failed(UserNotFoundException(Some(userId), None, s"failed to add user to the newsletter list, newsletterId=$newsletterId, userId=$userId"))
      recipient <- (recipients returning recipients) += DBRecipient(None, newsletterId, userId, status)
    } yield recipientCast(recipient)
    db.run(recipientAdd.transactionally)
  }

  private def getStatus(userStatus: UserStatus.Value, recipientStatus: Recipient.Status.Value): Recipient.Status.Value = {
    userStatus match {
      case UserStatus.NEW => recipientStatus
      case UserStatus.VERIFIED => recipientStatus
      case UserStatus.BLOCKED => Recipient.Status.REMOVED // don't subscribe blocked users
    }
  }

  def updateStatus(newsletterId: Long, userId: Long, status: Recipient.Status.Value) = db.run {
    recipients
      .filter(r => r.newsletterId === newsletterId && r.userId === userId)
      .map(_.status)
      .update(status)
  }
}
