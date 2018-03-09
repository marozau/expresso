package jobs.campaign

import models.Campaign

/**
  * @author im.
  */
object CampaignJob {

  def group = "campaign"

  def identity(c: Campaign) = s"$group-${c.newsletterId}-${c.editionId}"
}
