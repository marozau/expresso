package exceptions

/**
  * @author im.
  */
case class UserNotFoundException(message: String) extends BaseException {
  override def code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.USER_NOT_FOUND
}
