package exceptions

import models.Campaign

/**
  * @author im.
  */
case class InvalidCampaignStatusException(userId: Long, campaignId: Long, status: Campaign.Status.Value, message: String) extends BaseException {
  override def code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.INVALID_CAMPAIGN_STATUS
}
