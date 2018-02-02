package exceptions

/**
  * @author im.
  */
case class RecipientListNotFoundException(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.RECIPIENT_LIST_NOT_FOUND
}
