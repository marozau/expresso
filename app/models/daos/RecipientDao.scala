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

  implicit def recipientCast(r: DBRecipient) = Recipient(r.newsletterId, r.userId, r.status)

  // only editor must has access to this method
  def getByNewsletterId(newsletterId: Long) = db.run {
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

  def add(newsletterId: Long, userId: Long): Future[Recipient] = {
    def getStatus(status: UserStatus.Value): Recipient.Status.Value = {
      status match {
        case UserStatus.NEW => Recipient.Status.PENDING
        case UserStatus.VERIFIED => Recipient.Status.SUBSCRIBED
        case UserStatus.BLOCKED =>
          Logger.info(s"blocked user is trying to subscribe on newsletter, userId=$userId, newsletterId=$newsletterId")
          throw InvalidUserStatusException(userId, status, "blocked user is trying to subscribe on newsletter")
      }
    }
    val recipientAdd = for {
      userOption <- users.filter(_.id === userId).result.headOption
      status <- if (userOption.isDefined) DBIO.successful(getStatus(userOption.get.status))
        else DBIO.failed(UserNotFoundException(Some(userId), None, s"failed to add user to the newsletter list, newsletterId=$newsletterId"))
      recipient <- (recipients returning recipients) += DBRecipient(newsletterId, userId, status)
    } yield recipientCast(recipient)

    db.run(recipientAdd.transactionally)
  }
}
