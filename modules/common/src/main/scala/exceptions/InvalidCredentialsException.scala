package exceptions

/**
  * @author im.
  */
case class InvalidCredentialsException(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.INVALID_CREDENTIALS
}
