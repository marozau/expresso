package exceptions

/**
  * @author im.
  */
case class InternalServerError(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.INTERNAL_SERVER_ERROR
}
