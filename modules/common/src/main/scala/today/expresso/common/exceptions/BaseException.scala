package today.expresso.common.exceptions

/**
  * @author im.
  */
object BaseException {

  object ErrorCode extends Enumeration {
    type ErrorCode = Value
    val INTERNAL_SERVER_ERROR = Value
    val TIMEOUT = Value
    val VALIDATION_ERROR = Value
    val AUTHORIZATION = Value

    val POST_NOT_FOUND = Value
    val POST_TITLE_DUPLICATION = Value

    val RECIPIENT_NOT_FOUND = Value
    val INVALID_RECIPIENT_STATUS = Value

    val NEWSLETTER_NOT_FOUND = Value
    val NEWSLETTER_ALREADY_EXISTS = Value
    val NEWSLETTER_NAME_URL_ALREADY_EXIST = Value
    val ARCHIVE_NOT_FOUND = Value

    val CAMPAIGN_NOT_FOUND = Value
    val CAMPAIGN_ALREADY_SENT = Value
    val INVALID_CAMPAIGN_STATUS = Value
    val INVALID_CAMPAIGN_SCHEDULE = Value

    val EMAIL_NOT_FOUND = Value
    val INVALID_USER_STATUS = Value

    val USER_NOT_FOUND = Value
    val USER_ALREADY_EXISTS = Value
    val USER_UNVERIFIED = Value
    val INVALID_CREDENTIALS = Value
    val INVALID_EMAIL = Value

    val INVALID_AUTH_TOKEN = Value

    val EDITION_NOT_FOUND = Value
    val EDITION_ALREADY_EXISTS = Value

    val PAYMENT_METHOD_NOT_FOUND = Value
  }
}

trait BaseException extends Exception {
  val code: BaseException.ErrorCode.Value
  val message: String

  override def getMessage: String = message
}

trait BaseExceptionReflection {
  def throwException(code: String, message: () => String): Unit
}
