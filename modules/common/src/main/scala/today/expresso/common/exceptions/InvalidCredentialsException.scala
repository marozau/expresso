package today.expresso.common.exceptions

/**
  * @author im.
  */
case class InvalidCredentialsException(message: String) extends BaseException {
  override val code: today.expresso.common.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.INVALID_CREDENTIALS
}
