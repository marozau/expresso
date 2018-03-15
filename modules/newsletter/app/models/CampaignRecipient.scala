package models

import java.util.UUID

/**
  * @author im.
  */
case class CampaignRecipient(userId: Long,
                             recipientId: UUID,
                             editionId: Long,
                             status: Campaign.Status.Value,
                             attempts: Int,
                             reason: Option[String])


case class CampaignRecipientStatistics(editionId: Long,
                                       count: Int,
                                       sending: Int,
                                       sent: Int,
                                       failed: Int)
