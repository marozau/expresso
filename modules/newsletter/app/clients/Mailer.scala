package clients

import java.lang.invoke.MethodHandles
import javax.inject.{Inject, Singleton}

import today.expresso.common.exceptions.EmailSendException
import org.slf4j.LoggerFactory
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.mailer._

/**
  * @author im.
  */
object Mailer {

  sealed trait MailerEmail

  case class EmailHtml(
                        campaignId: Long,
                        userId: Long,
                        subject: String,
                        to: Seq[String],
                        fromName: String,
                        fromEmail: String,
                        bodyHtml: String) extends MailerEmail

}

@Singleton
class Mailer @Inject()(configuration: Configuration, mailerClient: MailerClient)(implicit ec: ExecutionContext) {

  import Mailer._

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  private val config = configuration.get[Configuration]("play.mailer")
  private val bounceAddress = config.get[String]("email.bounce")

  def send(email: EmailHtml): Future[String] = {
    logger.info(s"email send, userId=${email.userId}, campaignId=${email.campaignId}")
    val mailToSent = Email(
      subject = email.subject,
      from = s"${email.fromName} <${email.fromEmail}>",
      to = email.to,
      bodyHtml = Some(email.bodyHtml),
      replyTo = Seq(email.fromEmail),
      bounceAddress = Some(bounceAddress),
      headers = Map.empty.toSeq //TODO
    )
    Future(mailerClient.send(mailToSent))
      .recover {
        case t: Throwable =>
          logger.error("failed to send email", t)
          throw EmailSendException(s"email send failed, userId=${email.userId}, campaignId=${email.campaignId}")
      }
  }
}
