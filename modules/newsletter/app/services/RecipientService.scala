package services

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.{Recipient, UserRole, UserStatus}
import models.daos.RecipientDao

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class RecipientService @Inject()(recipientsDao: RecipientDao, userService: UserService)(implicit ec: ExecutionContext) {

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

  def subscribeEmail(email: String, newsletterId: Long) = {
    userService.getOrCreateByEmail(email)
      .flatMap{ user =>

      }
//    val loginInfo = LoginInfo(CredentialsProvider.ID, email)
//    userService.getOrCreate(loginInfo)
//      .flatMap { user =>
//        recipientsDao.add(newsletterId, user.id.get, Recipient.Status.PENDING).map((user, _))
//      }
//      .flatMap { case (user, recipient) =>
//        if (recipient.status == Recipient.Status.PENDING) {
//          recipient.id.get.getMostSignificantBits
//          mailService.sendVerification(user, recipient)
//            .map(_ => recipient)
//        } else Future.successful(recipient)
//      }
  }

  def verify(newsletterId: Long, userId: Long) = {
    recipientsDao.updateStatus(newsletterId, userId, Recipient.Status.SUBSCRIBED)
  }

  def unsubscribe(newsletterId: Long, userId: Long) = {
    recipientsDao.updateStatus(newsletterId, userId, Recipient.Status.UNSUBSCRIBED)
  }

  private def getStatus(userStatus: UserStatus.Value, recipientStatus: Recipient.Status.Value): Recipient.Status.Value = {
    userStatus match {
      case UserStatus.NEW => recipientStatus
      case UserStatus.VERIFIED => recipientStatus
      case UserStatus.BLOCKED => Recipient.Status.REMOVED // don't subscribe blocked users
    }
  }
}
