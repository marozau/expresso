package services

import javax.inject.{Inject, Singleton}

import events.newsletter.Subscribe
import models.{Recipient, User}
import play.api.libs.mailer.{Email, MailerClient}
import utils.UrlUtils

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class MailService @Inject()(
                             mailerClient: MailerClient,
                             newsletterService: NewsletterService,
                             urlUtils: UrlUtils)(implicit ex: ExecutionContext) {

  //TODO: bounce address on the mail client side

  def sendVerification(user: User, recipient: Recipient) = {
    val event = Subscribe(recipient.id.get.getMostSignificantBits, recipient.id.get.getLeastSignificantBits, user.id.get, recipient.newsletterId)
    val call = controllers.routes.EventController.subscribe(event)
    val url = urlUtils.absoluteURL(call)

    newsletterService.getById(recipient.newsletterId)
      .map { newsletter =>
        mailerClient.send(Email(
          subject = s"Verify Your Email Address for ${newsletter.name}",
          from = newsletter.email,
          to = Seq(user.email),
          replyTo = Seq(newsletter.email),
          bounceAddress = Some(newsletter.email),
          bodyText = Some(
            s"""
               |Click <a href="$url">here</a> to subscribe
               |""".stripMargin
          )
          //                bodyText = Some(views.txt.emails.signUp(user, url).body),
          //                bodyHtml = Some(views.html.emails.signUp(user, url).body)
        ))
      }
  }
}
