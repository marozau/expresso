package exceptions

/**
  * @author im.
  */
case class NewsletterNameUrlAlreadyExistException(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.NEWSLETTER_NAME_URL_ALREADY_EXIST
}
