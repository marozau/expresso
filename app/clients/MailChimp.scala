package clients

import java.lang.invoke.MethodHandles
import javax.inject.{Inject, Singleton}

import org.slf4j.LoggerFactory
import play.api.Configuration
import play.api.http.Status
import play.api.libs.json.{JsError, JsResult, Json}
import play.api.libs.ws._

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class MailChimp @Inject()()(ws: WSClient, config: Configuration)(implicit ec: ExecutionContext) {

  import MailChimp._
  import MailChimpApi._
  import MailChimpMarshaller._

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  private val mailChimpConfig = config.get[Configuration]("mailchimp")
  private val user = mailChimpConfig.get[String]("user")
  private val dc = mailChimpConfig.get[String]("dc")
  private val keyx = mailChimpConfig.get[String]("key")
  private val key = s"$keyx-$dc"

  private val apiUrl = s"https://$dc.api.mailchimp.com/3.0"

  //TODO: create custom dispatcher with maximum 10 threads and test if connection count exceeds this limit
  private def get(url: String, params: Seq[(String, String)] = Seq.empty): Future[WSResponse] = {
    ws.url(s"$apiUrl$url")
      .withHttpHeaders("content-type" -> "application/json")
      .withAuth(user, key, WSAuthScheme.BASIC)
      .addQueryStringParameters(params: _*)
      .get()
      .map { response =>
        logger.info("response={}", response)
        if (response.status != Status.OK) {
          val error: Error = response.json.validate[Error]
          throw new MailChimpException(response.status, error, s"$url request failed")
        } else response
      }
  }


  def getCampaigns(listId: String): Future[Seq[Campaign]] = {
    get("/campaigns", Seq("count" -> Int.MaxValue.toString, "list_id" -> listId))
      .map { response =>
        (response.json \ "campaigns").validate[Seq[Campaign]]
      }
  }

  def getCampaignInfo(campaignId: String): Future[CampaignContent] = {
    get(s"/campaigns/$campaignId")
      .map { response =>
        response.json.validate[CampaignContent]
      }
  }

  def getCampaignContent(campaignId: String): Future[CampaignContent] = {
    get(s"/campaigns/$campaignId/content")
      .map { response =>
        response.json.validate[CampaignContent]
      }
  }

  def getLists(): Future[Seq[List]] = {
    get("/lists", Seq("count" -> Int.MaxValue.toString))
      .map { response =>
        (response.json \ "lists").validate[Seq[List]]
      }
  }

  def getMembers(listId: String): Future[Seq[Member]] = {
    get(s"/lists/$listId/members", Seq("count" -> Int.MaxValue.toString))
      .map{ response =>
        (response.json \ "members").validate[Seq[Member]]
      }
  }

  def getReportsEmailActivity(campaignId: String): Future[Seq[EmailActivity]] = {
    get(s"/reports/${campaignId}/email-activity", Seq("count" -> Int.MaxValue.toString))
      .map { response =>
        (response.json \ "emails").validate[Seq[EmailActivity]]
      }
  }

  def getCampaignSubscriberActivity(campaignId: String, includeEmpty: Boolean): Future[Array[Map[String, Seq[Activity]]]] = {
    ws.url(s"https://$dc.api.mailchimp.com/export/1.0/campaignSubscriberActivity/")
      .withHttpHeaders("content-type" -> "application/json")
      .addQueryStringParameters("apikey" -> s"$key")
      .addQueryStringParameters("id" -> campaignId)
      .addQueryStringParameters("include_empty" -> includeEmpty.toString)
      .get()
      .map { response =>
        logger.info("response={}", response)
        if (response.status != Status.OK) {
          val error: Error = response.json.validate[Error]
          throw new MailChimpException(response.status, error, s"campaignSubscriberActivity request failed")
        } else response
      }
      .map { response =>
          response.body.split("\n").map{ activity =>
            Json.parse(activity).as[Map[String, Seq[Activity]]]
          }
      }
  }

}

object MailChimp {

  class MailChimpException(status: Int, error: MailChimpApi.Error, message: String) extends Exception(message)

  implicit def jsonResult[A](result: JsResult[A]): A = result match {
    case e: JsError => throw new RuntimeException("failed to parse mailchimp response, error=" + e)
    case r: JsResult[_] => r.get
  }
}

object MailChimpApi {

  case class Recipient(
                        listId: String,
                        listName: String,
                        segmentText: String,
                        recipientCount: Int)

  case class Settings(
                       subjectLine: Option[String],
                       title: String,
                       fromName: String,
                       replyTo: String,
                       useConversation: Boolean,
                       toName: String,
                       folderId: String,
                       authenticate: Boolean,
                       autoFooter: Boolean,
                       inlineCss: Boolean,
                       autoTweet: Boolean,
                       autoFbPost: Option[String],
                       fbComments: Boolean,
                       timewarp: Boolean,
                       templateId: Int,
                       dragAndDrop: Boolean)

  case class Tracking(
                       opens: Boolean,
                       htmlClicks: Boolean,
                       textClicks: Boolean,
                       goalTracking: Boolean,
                       ecomm360: Boolean,
                       googleAnalytics: String,
                       clicktale: String)

  case class Ecommerce(
                        totalOrders: Int,
                        totalSpent: BigDecimal,
                        totalRevenue: BigDecimal)

  case class ReportSummary(
                            opens: Int,
                            uniqueOpens: Int,
                            openRate: BigDecimal,
                            clicks: Int,
                            subscriberClicks: Int,
                            clickRate: BigDecimal,
                            ecommerce: Ecommerce
                          )

  case class DeliveryStatus(
                             enabled: Boolean,
                             canCancel: Option[Boolean],
                             status: Option[String],
                             emailsSent: Option[Int],
                             emailsCanceled: Option[Int]
                           )

  case class Campaign(
                       id: String,
                       typex: String,
                       createTime: String,
                       archiveUrl: String,
                       status: String,
                       emailsSent: Int,
                       sendTime: String,
                       contentType: String,
                       recipients: Recipient,
                       settings: Settings,
                       tracking: Tracking,
                       reportSummary: Option[ReportSummary],
                       deliveryStatus: DeliveryStatus
                     )

  case class VariateContent(
                             contentLabel: String,
                             plainText: String,
                             html: String)


  case class CampaignContent(
                              variateContents: Option[Seq[VariateContent]],
                              plainText: String,
                              html: String,
                              archiveHtml: String)

  // TODO: add other fields
  case class List(
                   id: String,
                   web_id: Int,
                   name: String)


  case class Links(
                    rel: String,
                    href: String,
                    method: String,
                    targetSchema: String,
                    schema: String)

  case class Error(
                    typex: String,
                    title: String,
                    status: Int,
                    detail: String,
                    instance: String)

  case class EmailActivity(
                            campaignId: String,
                            listId: String,
                            emailId: String,
                            emailAddress: String,
                            activity: Seq[Activity])

  case class Activity(
                       action: String,
                       typex: Option[String],
                       timestamp: String,
                       url: Option[String],
                       ip: Option[String])

  case class Member(
                     id: String,
                     emailAddress: String,
                     uniqueEmailId: String,
                     emailType: String,
                     status: String,
                     unsubscribeReason: Option[String],
                     mergeFields: Option[Map[String, String]],
                     interests: Option[String],
                     ipSignup: Option[String],
                     timestampSignup: String,
                     ipOpt: Option[String],
                     timestampOpt: String,
                     memberRating: Int,
                     lastChanged: String,
                     language: Option[String],
                     vip: Option[Boolean],
                     emailClient: Option[String],
                     listId: Option[String])
}

object MailChimpMarshaller {

  import MailChimpApi._
  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  implicit val recipientReads: Reads[Recipient] = (
    (__ \ "list_id").read[String] and
      (__ \ "list_name").read[String] and
      (__ \ "segment_text").read[String] and
      (__ \ "recipient_count").read[Int]
    ) (Recipient.apply _)

  implicit val settingsReads: Reads[Settings] = (
    (__ \ "subject_line").readNullable[String] and
      (__ \ "title").read[String] and
      (__ \ "from_name").read[String] and
      (__ \ "reply_to").read[String] and
      (__ \ "use_conversation").read[Boolean] and
      (__ \ "to_name").read[String] and
      (__ \ "folder_id").read[String] and
      (__ \ "authenticate").read[Boolean] and
      (__ \ "auto_footer").read[Boolean] and
      (__ \ "inline_css").read[Boolean] and
      (__ \ "auto_tweet").read[Boolean] and
      (__ \ "auto_fb_post").readNullable[String] and
      (__ \ "fb_comments").read[Boolean] and
      (__ \ "timewarp").read[Boolean] and
      (__ \ "template_id").read[Int] and
      (__ \ "drag_and_drop").read[Boolean]
    ) (Settings.apply _)

  implicit val trackingReads: Reads[MailChimpApi.Tracking] = (
    (__ \ "opens").read[Boolean] and
      (__ \ "html_clicks").read[Boolean] and
      (__ \ "text_clicks").read[Boolean] and
      (__ \ "goal_tracking").read[Boolean] and
      (__ \ "ecomm360").read[Boolean] and
      (__ \ "google_analytics").read[String] and
      (__ \ "clicktale").read[String]
    ) (Tracking.apply _)

  implicit val EcommerceReads: Reads[Ecommerce] = (
    (__ \ "total_orders").read[Int] and
      (__ \ "total_spent").read[BigDecimal] and
      (__ \ "total_revenue").read[BigDecimal]
    ) (Ecommerce.apply _)

  implicit val ReportSummaryReads: Reads[ReportSummary] = (
    (__ \ "opens").read[Int] and
      (__ \ "unique_opens").read[Int] and
      (__ \ "open_rate").read[BigDecimal] and
      (__ \ "clicks").read[Int] and
      (__ \ "subscriber_clicks").read[Int] and
      (__ \ "click_rate").read[BigDecimal] and
      (__ \ "ecommerce").read[Ecommerce]
    ) (ReportSummary.apply _)

  implicit val DeliveryStatusReads: Reads[DeliveryStatus] = (
    (__ \ "enabled").read[Boolean] and
      (__ \ "can_cancel").readNullable[Boolean] and
      (__ \ "status").readNullable[String] and
      (__ \ "emails_sent").readNullable[Int] and
      (__ \ "emails_canceled").readNullable[Int]
    ) (DeliveryStatus.apply _)

  implicit val campaignReads: Reads[Campaign] = (
    (__ \ "id").read[String] and
      (__ \ "type").read[String] and
      (__ \ "create_time").read[String] and
      (__ \ "archive_url").read[String] and
      (__ \ "status").read[String] and
      (__ \ "emails_sent").read[Int] and
      (__ \ "send_time").read[String] and
      (__ \ "content_type").read[String] and
      (__ \ "recipients").read[Recipient] and
      (__ \ "settings").read[Settings] and
      (__ \ "tracking").read[Tracking] and
      (__ \ "report_summary").readNullable[ReportSummary] and
      (__ \ "delivery_status").read[DeliveryStatus]
    ) (Campaign.apply _)

  implicit val variateContentReads: Reads[VariateContent] = (
    (__ \ "content_label").read[String] and
      (__ \ "plain_text").read[String] and
      (__ \ "html").read[String]
    ) (VariateContent.apply _)

  implicit val LinksReads: Reads[Links] = (
    (__ \ "rel").read[String] and
      (__ \ "href").read[String] and
      (__ \ "method").read[String] and
      (__ \ "targetSchema").read[String] and
      (__ \ "schema").read[String]
    ) (Links.apply _)

  implicit val campaignContentReads: Reads[CampaignContent] = (
    (__ \ "variate_contents").readNullable[Seq[VariateContent]] and
      (__ \ "plain_text").read[String] and
      (__ \ "html").read[String] and
      (__ \ "archive_html").read[String]
    ) (CampaignContent.apply _)

  implicit val listReads: Reads[List] = (
    (__ \ "id").read[String] and
      (__ \ "web_id").read[Int] and
      (__ \ "name").read[String]
    ) (List.apply _)

  implicit val errorReads: Reads[Error] = (
    (__ \ "type").read[String] and
      (__ \ "title").read[String] and
      (__ \ "status").read[Int] and
      (__ \ "detail").read[String] and
      (__ \ "instance").read[String]
    ) (Error.apply _)

  implicit val activityReads: Reads[Activity] = (
    (__ \ "action").read[String] and
      (__ \ "type").readNullable[String] and
      (__ \ "timestamp").read[String] and
      (__ \ "url").readNullable[String] and
      (__ \ "ip").readNullable[String]
    ) (Activity.apply _)

  implicit val emailActivityReads: Reads[EmailActivity] = (
    (__ \ "campaign_id").read[String] and
      (__ \ "list_id").read[String] and
      (__ \ "email_id").read[String] and
      (__ \ "email_address").read[String] and
      (__ \ "activity").read[Seq[Activity]]
    ) (EmailActivity.apply _)

  implicit val memberReads: Reads[Member] = (
    (__ \ "id").read[String] and
      (__ \ "email_address").read[String] and
      (__ \ "unique_email_id").read[String] and
      (__ \ "email_type").read[String] and
      (__ \ "status").read[String] and
      (__ \ "unsubscribe_reason").readNullable[String] and
      (__ \ "merge_fields").readNullable[Map[String, String]] and
      (__ \ "interests").readNullable[String] and
      (__ \ "ip_signup").readNullable[String] and
      (__ \ "timestamp_signup").read[String] and
      (__ \ "ip_opt").readNullable[String] and
      (__ \ "timestamp_opt").read[String] and
      (__ \ "member_rating").read[Int] and
      (__ \ "last_changed").read[String] and
      (__ \ "language").readNullable[String] and
      (__ \ "vip").readNullable[Boolean] and
      (__ \ "email_client").readNullable[String] and
      (__ \ "list_id").readNullable[String]
    )(Member.apply _)

}

