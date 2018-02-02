package exceptions

/**
  * @author im.
  */
case class InvalidCampaignStatusException(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.INVALID_CAMPAIGN_STATUS
}
