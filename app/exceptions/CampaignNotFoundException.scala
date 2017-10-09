package exceptions

/**
  * @author im.
  */
case class CampaignNotFoundException(id: Long, message: String) extends BaseException {
  override def code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.CAMPAIGN_NOT_FOUND
}
