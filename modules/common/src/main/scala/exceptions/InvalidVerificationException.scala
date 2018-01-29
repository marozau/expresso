package exceptions

/**
  * @author im.
  */
case class InvalidVerificationException(message: String) extends BaseException {
  override def code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.INVALID_VERIFICATION
}
