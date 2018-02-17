package exceptions

/**
  * @author im.
  */
case class NewsletterNotFoundException(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.NEWSLETTER_NOT_FOUND
}

object NewsletterNotFoundException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.NEWSLETTER_NOT_FOUND.toString))
      throw NewsletterNotFoundException(message())
  }
}
