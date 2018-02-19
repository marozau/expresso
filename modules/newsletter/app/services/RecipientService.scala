package services

import java.util.UUID
import javax.inject.{Inject, Singleton}

import exceptions.InvalidUserStatusException
import models.Recipient
import models.daos.RecipientDao
import today.expresso.grpc.user.domain.User

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class RecipientService @Inject()(recipientsDao: RecipientDao, userService: UserService, mailService: MailService)(implicit ec: ExecutionContext) {

  def getByNewsletterId(newsletterId: Long, status: Recipient.Status.Value) = {
    recipientsDao.getByNewsletterId(newsletterId, None, Some(status))
  }

  def getByNewsletterId(userId: Long, newsletterId: Long, status: Option[Recipient.Status.Value]) = {
    recipientsDao.getByNewsletterId(newsletterId, Some(userId), status)
  }

  /**
    * Method is only for future API calls. Allows user to subscribe on newsletter without subscription verification
    *
    * @param userId       THe user id
    * @param newsletterId The newsletter user wants to subscribe
    * @return The recipient information
    */
  def subscribeUser(userId: Long, newsletterId: Long) = {
    recipientsDao.add(userId, newsletterId, Some(Recipient.Status.SUBSCRIBED))
  }

  // TODO: in case of failure - retry, all services should be idempotent
  def subscribeEmail(email: String, newsletterId: Long) = {
    userService.createReader(email)
      .flatMap { user =>
        if (user.status == User.Status.BLOCKED) throw InvalidUserStatusException("failed to subscribe, user is blocked")
        else recipientsDao.add(user.id, newsletterId).map((user, _))
      }
      .flatMap { case (user, recipient) =>
        if (recipient.status == Recipient.Status.PENDING) {
          mailService.sendVerification(email, user, recipient)
            .map(_ => recipient)
        } else Future.successful(recipient)
      }
  }

  def verifySubscription(recipientId: UUID) = {
    recipientsDao.subscribe(recipientId) //TODO: event
  }

  def unsubscribe(recipientId: UUID) = {
    recipientsDao.unsubscribe(recipientId) //TODO: event
  }

  def remove(recipientId: UUID) = {
    recipientsDao.remove(recipientId) //TODO: event
  }

  def clean(recipientId: UUID) = {
    recipientsDao.clean(recipientId) //TODO: event
  }

  def spam(recipientId: UUID) = {
    recipientsDao.spam(recipientId) //TODO: event
  }
}
