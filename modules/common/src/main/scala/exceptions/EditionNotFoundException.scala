package exceptions

/**
  * @author im.
  */
case class EditionNotFoundException(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.POST_NOT_FOUND
}
