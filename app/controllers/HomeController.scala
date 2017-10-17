package controllers

import javax.inject._

import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.db.Database
import models.repositories.{MailChimpRepository, UserRepository}
import services.{Elasticsearch, MailChimp}

import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(components: ControllerComponents, mailChimp: MailChimp, elasticsearch: Elasticsearch, repo: UserRepository, mailChimpRepo: MailChimpRepository)(implicit ec: ExecutionContext)
  extends AbstractController(components) with I18nSupport {

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def test = Action.async {
    mailChimp.getCampaigns("fb75c0e47f")
      .map(response => Ok(response.toString))
  }

  def test2 = Action.async {
    mailChimp.getCampaignContent("fb75c0e47f")
      .map(response => Ok(response.toString))
  }

  def test3 = Action.async {
    mailChimp.getLists()
      .map(response => Ok(response.toString))
  }

  import Elasticsearch._

  def test4 = Action.async {
    mailChimp.getCampaignContent("fb75c0e47f")
      .map { response =>
        //        elasticsearch.index(ExpressoIndex("0030da93d8", "http://hello.world", "2015-09-15T14:40:36+00:00", "привет!"))
        Ok(response.toString)
      }
  }

  def test5 = Action.async {
    import scala.concurrent.duration._
    mailChimp.getCampaigns("fb75c0e47f")
      .map { campaigns =>
        campaigns
          .grouped(10)
          .foreach { campaignGrouped =>
            Await.result(Future.sequence(
              campaignGrouped.map { campaign =>
                mailChimp.getCampaignContent(campaign.id)
                  .map { content =>
                    elasticsearch.index(ExpressoIndex(campaign.id, campaign.archiveUrl, campaign.sendTime, content.plainText))
                  }
              }
            ).recover {
              case t: Throwable => Logger.error("failed to index document", t)
            }, 1.minute)
            Logger.info("completed: 10")
          }
      }.map(r => Ok("Все заиндексировано!"))
      .recover {
        case t: Throwable => Ok("Ошибка:" + t.getMessage)
      }
  }

  def test6 = Action.async {
    mailChimp.getCampaignSubscriberActivity("fdd4833d93", includeEmpty = false)
      .map { response =>
        Ok(response.mkString(","))
      }
  }

  def test7 = Action.async {
    mailChimp.getReportsEmailActivity("fdd4833d93")
      .map { response =>
        Ok(response.toString)
      }
  }

  def loadMailChimpUsers = Action.async {
    mailChimp.getMembers("fb75c0e47f")
      .flatMap { response =>
        mailChimpRepo.saveMembers(response)
          .map { count => Ok(count.mkString(",")) }
      }
  }

  def loadSubscriberActivity(campaignId: String) = Action.async {
    loadActivity(campaignId).map { count => Ok(count.mkString(",")) }
  }

  private def loadActivity(campaignId: String) = {
    mailChimp.getCampaignSubscriberActivity(campaignId, includeEmpty = false)
      .flatMap { activities =>
        mailChimpRepo.saveSubscirberActivity(campaignId, activities)
      }
  }

  def loadSubscriberActivityAll() = Action.async {
    import scala.concurrent.duration._
    mailChimp.getCampaigns("fb75c0e47f")
      .map { campaigns =>
        campaigns
          .grouped(10)
          .foreach { campaignGrouped =>
            Await.result(Future.sequence(
              campaignGrouped.map { campaign =>
                loadActivity(campaign.id)
              }
            ).recover {
              case t: Throwable => Logger.error("failed to index document", t)
            }, 1.minute)
          }
      }.map { _ => Ok("хорошо") }
  }

  def loadCampaigns = Action.async {
    mailChimp.getCampaigns("fb75c0e47f")
      .flatMap { response =>
        mailChimpRepo.saveCampaigns(response)
          .map { _ => Ok("хорошо") }
      }
  }


}
