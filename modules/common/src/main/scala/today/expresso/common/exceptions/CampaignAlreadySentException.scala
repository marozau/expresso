package today.expresso.common.exceptions

/**
  * @author im.
  */
case class CampaignAlreadySentException(message: String) extends BaseException {
  override val code: today.expresso.common.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.CAMPAIGN_ALREADY_SENT
}

object CampaignAlreadySentException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.CAMPAIGN_ALREADY_SENT.toString))
      throw CampaignAlreadySentException(message())
  }
}