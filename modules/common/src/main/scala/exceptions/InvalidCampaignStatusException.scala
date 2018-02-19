package exceptions

/**
  * @author im.
  */
case class InvalidCampaignStatusException(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.INVALID_CAMPAIGN_STATUS
}

object InvalidCampaignStatusException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.INVALID_CAMPAIGN_STATUS.toString))
      throw InvalidCampaignStatusException(message())
  }
}