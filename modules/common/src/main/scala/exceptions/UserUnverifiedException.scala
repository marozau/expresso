package exceptions

/**
  * @author im.
  */
case class UserUnverifiedException(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.USER_UNVERIFIED
}
