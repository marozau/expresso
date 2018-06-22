package models.components

import today.expresso.common.db.Repository
import slick.jdbc.GetResult
import today.expresso.stream.domain.model.newsletter.Campaign

/**
  * @author im.
  */
trait CampaignComponent {
  this: Repository =>

  import api._

  implicit val campaignStatusMapper = createEnumJdbcType("CAMPAIGN_STATUS", Campaign.Status)

  implicit val campaignGetResult: GetResult[Campaign] = GetResult { r =>
    Campaign(
      r.nextLong(),
      r.nextLong(),
      r.nextLong(),
      r.nextTimestamp().toInstant,
      campaignStatusMapper.getValue(r.rs, r.skip.currentPos),
      r.nextStringOption(),
      Option(playJsonTypeMapper.getValue(r.rs, r.skip.currentPos))
    )
  }
}
