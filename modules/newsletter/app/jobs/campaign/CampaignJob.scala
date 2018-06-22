package jobs.campaign

import today.expresso.stream.domain.model.newsletter.Campaign

/**
  * @author im.
  */
object CampaignJob {

  def group = "campaign"

  def identity(c: Campaign) = s"$group-${c.newsletterId}-${c.editionId}"

  def userGroup(userId: Long) = s"$group-$userId"
}
