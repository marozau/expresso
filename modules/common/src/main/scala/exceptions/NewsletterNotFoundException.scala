package exceptions

/**
  * @author im.
  */
case class NewsletterNotFoundException(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.NEWSLETTER_NOT_FOUND
}
