package models.components

import slick.jdbc.GetResult
import today.expresso.common.db.Repository
import today.expresso.stream.domain.model.newsletter.{CampaignRecipient, CampaignRecipientStatistics}

/**
  * @author im.
  */
trait CampaignRecipientComponent extends CampaignComponent {
  this: Repository =>

  import api._

  implicit val campaignRecipientGetResult: GetResult[CampaignRecipient] = GetResult { r =>
    CampaignRecipient(
      r.nextLong(),
      uuidColumnType.getValue(r.rs, r.skip.currentPos),
      r.nextLong(),
      campaignStatusMapper.getValue(r.rs, r.skip.currentPos),
      r.nextInt(),
      r.nextStringOption()
    )
  }

  implicit val campaignRecipientStatisticsGetResult: GetResult[CampaignRecipientStatistics] = GetResult { r =>
    CampaignRecipientStatistics(
      r.nextLong(),
      r.nextInt(),
      r.nextInt(),
      r.nextInt(),
      r.nextInt()
    )
  }
}
