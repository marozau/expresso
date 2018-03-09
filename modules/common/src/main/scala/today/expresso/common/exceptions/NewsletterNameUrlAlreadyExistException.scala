package today.expresso.common.exceptions

/**
  * @author im.
  */
case class NewsletterNameUrlAlreadyExistException(message: String) extends BaseException {
  override val code: today.expresso.common.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.NEWSLETTER_NAME_URL_ALREADY_EXIST
}
