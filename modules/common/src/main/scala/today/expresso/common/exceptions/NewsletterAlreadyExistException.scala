package today.expresso.common.exceptions

/**
  * @author im.
  */
case class NewsletterAlreadyExistException(message: String) extends BaseException {
  override val code: today.expresso.common.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.NEWSLETTER_ALREADY_EXISTS
}

object NewsletterAlreadyExistException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.NEWSLETTER_ALREADY_EXISTS.toString))
      throw NewsletterAlreadyExistException(message())
  }
}
