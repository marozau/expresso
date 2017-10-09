package repositories

import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import services.MailChimpApi.{Activity, Campaign, Member}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class MailChimpRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def saveMembers(members: Seq[Member]) = {
    db.run(DBIO.sequence(members.map { member =>
      sql"""INSERT INTO mc_member(
             id,
             email_address,
             unique_email_id,
             email_type,
             status,
             unsubscribe_reason,
             interests,
             ip_signup,
             timestamp_signup,
             ip_opt,
             timestamp_opt,
             member_rating,
             last_changed,
             language,
             vip,
             email_client,
             list_id
            ) VALUES
            (
              ${member.id},
              ${member.emailAddress},
              ${member.uniqueEmailId},
              ${member.emailType},
              ${member.status},
              ${member.unsubscribeReason},
              ${member.interests},
              ${member.ipSignup},
              ${member.timestampSignup},
              ${member.ipOpt},
              ${member.timestampOpt},
              ${member.memberRating},
              ${member.lastChanged},
              ${member.language},
              ${member.vip},
              ${member.emailClient},
              ${member.listId}
            ) ON CONFLICT DO NOTHING; SELECT 1;""".as[Int]
    }))
  }

  def saveCampaigns(campaigns: Seq[Campaign]) = {
    db.run(DBIO.sequence(campaigns.map { campaign =>
      sql"""INSERT INTO mc_campaign(
             id,
             type,
             create_time,
             archive_url,
             status,
             emails_sent,
             send_time,
             content_type,
             recipients_list_id,
             recipient_count
            ) VALUES
            (
              ${campaign.id},
              ${campaign.typex},
              ${campaign.createTime},
              ${campaign.archiveUrl},
              ${campaign.status},
              ${campaign.emailsSent},
              ${campaign.sendTime},
              ${campaign.contentType},
              ${campaign.recipients.listId},
              ${campaign.recipients.recipientCount}
            ) ON CONFLICT DO NOTHING; SELECT 1;""".as[Int]
    }))
  }

  def saveSubscirberActivity(campaignId: String, activities: Array[Map[String, Seq[Activity]]]) = {
    val t: Seq[(String, Activity)] = activities.flatMap { activity =>
      activity.flatMap { case (key, value) =>
        value.map { v => (key, v) }
      }
    }.toSeq
    db.run(DBIO.sequence(t.map { case (k, v) =>
      sql"""
                    INSERT INTO mc_activity_log(
                      campaign_id,
                      email_address,
                      action,
                      type,
                      timestamp,
                      url,
                      ip ) VALUES
                    (
                     ${campaignId},
                     ${k},
                     ${v.action},
                     ${v.typex},
                     ${v.timestamp},
                     ${v.url},
                     ${v.ip}
                    ) ON CONFLICT DO NOTHING; SELECT 1;""".as[Int]
    }))
  }
}
