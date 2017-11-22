package services

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.{Recipient, UserRole}
import models.daos.RecipientDao

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class RecipientService @Inject()(recipientsDao: RecipientDao, userService: UserService, mailService: MailService)(implicit ec: ExecutionContext) {

  def getNewsletterRecipients(newsletterId: Long) = {
    recipientsDao.getByNewsletterId(newsletterId)
  }

  def getEditionRecipients(editionId: Long) = recipientsDao.getByEditionId(editionId)

  /**
    * Method is only for future API calls. Allows user to subsribe on newsletter without subscription verification
    *
    * @param newsletterId The newsletter user wants to subscribe
    * @param userId       THe user id
    * @return The recipient information
    */
  //FIXME: Recipient status must be SUBSCRIBED instead of PENDING
  def addUser(newsletterId: Long, userId: Long) = {
    recipientsDao.add(newsletterId, userId, Recipient.Status.SUBSCRIBED)
  }

  def subscribe(newsletterId: Long, email: String, status: Recipient.Status.Value) = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, email)
    userService.getOrCreate(loginInfo, List(UserRole.READER))
      .flatMap { user =>
        recipientsDao.add(newsletterId, user.id.get, status).map((user, _))
      }
      .flatMap { case (user, recipient) =>
        if (recipient.status == Recipient.Status.PENDING) {
          recipient.id.get.getMostSignificantBits
          mailService.sendVerification(user, recipient)
            .map(_ => recipient)
        } else Future.successful(recipient)
      }
  }

  def verify(newsletterId: Long, userId: Long) = {
    recipientsDao.updateStatus(newsletterId, userId, Recipient.Status.SUBSCRIBED)
  }

  def unsubscribe(newsletterId: Long, userId: Long) = {
    recipientsDao.updateStatus(newsletterId, userId, Recipient.Status.UNSUBSCRIBED)
  }
}
