package exceptions

/**
  * @author im.
  */
case class RecipientNotFoundException(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.RECIPIENT_NOT_FOUND
}

object RecipientNotFoundException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.RECIPIENT_NOT_FOUND.toString))
      throw RecipientNotFoundException(message())
  }
}

