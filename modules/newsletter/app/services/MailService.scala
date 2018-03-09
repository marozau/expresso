package services

import javax.inject.{Inject, Singleton}

import models.Recipient
import play.api.Logger
import play.api.libs.mailer.{Email, MailerClient}
import today.expresso.grpc.user.domain.User
import today.expresso.common.utils.UrlUtils

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
trait MailService {
  def sendVerification(email: String, user: User, recipient: Recipient): Future[Unit]
}

@Singleton
class MailServiceImpl @Inject()(
                                 mailerClient: MailerClient,
                                 newsletterService: NewsletterService,
                                 urlUtils: UrlUtils)(implicit ex: ExecutionContext)
  extends MailService {

  //TODO: bounce address on the mail client side

  override def sendVerification(email: String, user: User, recipient: Recipient): Future[Unit] = {
    //    val event = Subscribe(recipient.id.getMostSignificantBits, recipient.id.getLeastSignificantBits, user.id, recipient.newsletterId)
    //    val call = controllers.routes.EventController.subscribe(event)
    //    val url = urlUtils.absoluteURL(call)

    Logger.info(s"send email: $email")
    Future.successful(Unit)
    //    newsletterService.getById(recipient.newsletterId)
    //      .map { newsletter =>
    //        mailerClient.send(Email(
    //          subject = s"Verify Your Email Address for ${newsletter.name}",
    //          from = newsletter.email,
    //          to = Seq(user.email),
    //          replyTo = Seq(newsletter.email),
    //          bounceAddress = Some(newsletter.email),
    //          bodyText = Some(
    //            s"""
    //               |Click <a href="$url">here</a> to subscribe
    //               |""".stripMargin
    //          )
    //          //                bodyText = Some(views.txt.emails.signUp(user, url).body),
    //          //                bodyHtml = Some(views.html.emails.signUp(user, url).body)
    //        ))
    //      }
  }
}
