package exceptions

/**
  * @author im.
  */
object BaseException {

  object ErrorCode extends Enumeration {
    type ErrorCode = Value
    val INTERNAL_SERVER_ERROR = Value
    val TIMEOUT = Value
    val VALIDATION_ERROR = Value
    val POST_NOT_FOUND = Value
    val EDITION_NOT_FOUND = Value
    val NEWSLETTER_NOT_FOUND = Value
    val FRAME_NOT_FOUND = Value
    val CAMPAIGN_NOT_FOUND = Value
    val RECIPIENT_LIST_NOT_FOUND = Value
    val USER_NOT_FOUND = Value
    val EMAIL_NOT_FOUND = Value
    val INVALID_CAMPAIGN_STATUS = Value
    val INVALID_USER_STATUS = Value
  }
}

trait BaseException extends Exception {
  def code: BaseException.ErrorCode.Value
  def message: String

  override def getMessage: String = message
}
