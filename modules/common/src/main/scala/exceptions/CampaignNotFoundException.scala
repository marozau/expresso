package exceptions

/**
  * @author im.
  */
case class CampaignNotFoundException(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.CAMPAIGN_NOT_FOUND
}
