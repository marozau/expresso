package exceptions

/**
  * @author im.
  */
case class NewsletterAlreadyExistException(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.NEWSLETTER_ALREADY_EXIST
}

object NewsletterAlreadyExistException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.NEWSLETTER_ALREADY_EXIST.toString))
      throw NewsletterAlreadyExistException(message())
  }
}
