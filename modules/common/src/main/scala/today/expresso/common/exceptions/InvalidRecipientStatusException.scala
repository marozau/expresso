package today.expresso.common.exceptions

/**
  * @author im.
  */
case class InvalidRecipientStatusException(message: String) extends BaseException {
override val code: today.expresso.common.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.INVALID_RECIPIENT_STATUS
}

object InvalidRecipientStatusException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.INVALID_RECIPIENT_STATUS.toString))
      throw InvalidRecipientStatusException(message())
  }
}
