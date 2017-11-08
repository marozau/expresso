package exceptions

import java.time.ZonedDateTime

/**
  * @author im.
  */
case class InvalidCampaignScheduleException(campaignId: Long, sendTime: ZonedDateTime, message: String) extends BaseException {
  override def code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.INVALID_CAMPAIGN_SCHEDULE
}
