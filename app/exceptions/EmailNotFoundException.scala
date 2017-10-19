package exceptions

/**
  * @author im.
  */
case class EmailNotFoundException(message: String) extends BaseException {
  override def code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.EMAIL_NOT_FOUND
}
