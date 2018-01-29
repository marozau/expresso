package services

import javax.inject.{Inject, Singleton}

import exceptions.NewsletterNotFoundException
import models.{Campaign, Edition, Newsletter, User}
import models.daos.{CampaignDao, EditionDao, NewsletterDao}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class NewsletterService @Inject()(newsletterDao: NewsletterDao, editionDao: EditionDao, campaignDao: CampaignDao)(implicit ec: ExecutionContext) {

  def getById(newsletterId: Long) = {
    newsletterDao.getById(newsletterId)
      .map { result =>
        if (result.isEmpty) throw NewsletterNotFoundException(s"getById failed, '$newsletterId' not found" )
        result.get
      }
  }

  def getByNameUrl(nameUrl: String) = {
    newsletterDao.getByNameUrl(nameUrl)
      .map { result =>
        if (result.isEmpty) throw NewsletterNotFoundException(s"getByNameUrl failed, '$nameUrl' not found")
        result.get
      }
  }

  def list(): Future[Seq[(Newsletter, Option[Edition], Option[Campaign])]] = {
    newsletterDao.list()
      .flatMap { list =>
        Future.sequence(
          list.map(newsletter => editionDao.getUnpublished(newsletter.id.get)
            .flatMap { editionOption =>
              if (editionOption.isDefined) campaignDao.getByEditionId(editionOption.get.id.get).map((newsletter, editionOption, _))
              else Future.successful((newsletter, None, None))
            }
          )
        )
      }
  }

  def create(newsletter: Newsletter) = newsletterDao.create(newsletter)
}
