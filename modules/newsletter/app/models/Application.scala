package models

import javax.inject.{Inject, Singleton}

import play.api.Configuration
import services._

/**
  * @author im.
  */
@Singleton
case class Application @Inject()(
                             configuration: Configuration,
                             userService: UserService,
                             recipientService: RecipientService,
                             newsletterService: NewsletterService,
                             editionService: EditionService,
                             campaignService: CampaignService,
                             mailService: MailService) {

}
