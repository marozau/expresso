package exceptions

/**
  * @author im.
  */
case class NewsletterAlreadyExistException(message: String) extends BaseException {
  override def code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.NEWSLETTER_ALREADY_EXIST
}
