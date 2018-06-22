package services

import java.util.UUID

import javax.inject.{Inject, Singleton}
import today.expresso.common.exceptions.InvalidUserStatusException
import models.daos.RecipientDao
import today.expresso.stream.domain.model.newsletter.Recipient
import today.expresso.stream.domain.model.user.User

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class RecipientService @Inject()(recipientsDao: RecipientDao, userService: UserService, mailService: MailService)(implicit ec: ExecutionContext) {

  def getByNewsletterId(userId: Long, newsletterId: Long, status: Option[Recipient.Status.Value]) = {
    recipientsDao.getByNewsletterId(userId, newsletterId, status)
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

  def updateStatus(recipientId: UUID, status: Recipient.Status.Value) = {
    recipientsDao.updateStatus(recipientId, status) //TODO: event
  }

  def startCampaign(userId: Long, campaignId: Long) = {
    recipientsDao.startCampaign(userId, campaignId)
  }

  def completeCampaign(userId: Long, campaignId: Long) = {
    recipientsDao.completeCampaign(userId, campaignId)
  }
}
