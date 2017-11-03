package exceptions

/**
  * @author im.
  */
case class RecipientListNotFoundException(id: Option[Long], userId: Option[Long], message: String) extends BaseException {
  override def code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.RECIPIENT_LIST_NOT_FOUND
}
