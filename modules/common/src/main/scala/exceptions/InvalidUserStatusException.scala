package exceptions

/**
  * @author im.
  */
case class InvalidUserStatusException(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.INVALID_USER_STATUS
}
