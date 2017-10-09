package services

import java.util.Date
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import com.microtripit.mandrillapp.lutung.MandrillApi
import com.microtripit.mandrillapp.lutung.view.MandrillMessage
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.{MergeVar, Recipient}
import play.api.Configuration

import scala.collection.JavaConverters._
import play.api.Logger

import scala.concurrent.Future

/**
  * @author im.
  */
trait MandrillEmailService {
  def sendEmail(email: String, template: String, params: Map[String, String]): Future[String]

  def schedule(email: String, template: String, params: Map[String, String], sendAt: Date): Future[String]
}

@Singleton
class Mandrill @Inject()(config: Configuration, actorSystem: ActorSystem) extends MandrillEmailService {

  private implicit val ec = actorSystem.dispatchers.lookup("mandrill.blocking-dispatcher")

  private val logger: Logger = Logger(this.getClass)

  private val mandrillApi = new MandrillApi(config.get[String]("mandrill.api_key"))
  private val emailFrom = config.get[String]("mandrill.email.from")
  private val mergeLang = config.get[String]("mandrill.email.merge_lang")

  override def sendEmail(email: String, template: String, params: Map[String, String] = Map.empty): Future[String] = {
    Future {
      val message = buildMessage(email, params)
      mandrillApi.messages.sendTemplate(template, null, message, false)
    }.map { statuses =>
      logger.info(s"Mandrill request: email=${statuses(0).getEmail}, status=${statuses(0).getStatus}, " +
        s"reject_reason=${statuses(0).getRejectReason}, id=${statuses(0).getId}")
      statuses(0).getId
    }
  }

  override def schedule(email: String, template: String, params: Map[String, String], sendAt: Date): Future[String] = {
    Future {
      val message = buildMessage(email, params)
      mandrillApi.messages.sendTemplate(template, null, message, false)
    }.map { statuses =>
      logger.info(s"Mandrill request: email=${statuses(0).getEmail}, status=${statuses(0).getStatus}, " +
        s"reject_reason=${statuses(0).getRejectReason}, id=${statuses(0).getId}")
      statuses(0).getId
    }
  }

  private def buildMessage(email: String, params: Map[String, String]) = {
    val recipient = new MandrillMessage.Recipient
    recipient.setEmail(email)
    recipient.setType(Recipient.Type.TO)
    val mergeVars = buildMergeVarBucketList(email, params)
    val message = new MandrillMessage
    message.setTo(List(recipient).asJava)
    message.setFromEmail(emailFrom)
//    message.setMerge(true)
//    message.setMergeLanguage(mergeLang)
//    message.setMergeVars(mergeVars)
    message
  }

  private def buildMergeVarBucketList(email: String, params: Map[String, String]) = {
    val mergeVars = List()
    params.map { case (k, v) => new MergeVar(k, v) }
    val mergeVarBucket = new MandrillMessage.MergeVarBucket
    mergeVarBucket.setRcpt(email)
    mergeVarBucket.setVars(mergeVars.toArray)
    List(mergeVarBucket)
  }
}
