package models

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import services._

/**
  * NOTE: StreamStarter instance is needed for lazy kafka stream start at the end of application initialization as other stream components are created eagerly
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
