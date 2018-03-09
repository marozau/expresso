package today.expresso.common.exceptions

/**
  * @author im.
  */
case class InvalidCampaignScheduleException(message: String) extends BaseException {
  override val code: today.expresso.common.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.INVALID_CAMPAIGN_SCHEDULE
}

object InvalidCampaignScheduleException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.INVALID_CAMPAIGN_SCHEDULE.toString))
      throw InvalidCampaignScheduleException(message())
  }
}