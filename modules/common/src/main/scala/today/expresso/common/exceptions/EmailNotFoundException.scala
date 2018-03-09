package today.expresso.common.exceptions

/**
  * @author im.
  */
case class EmailNotFoundException(message: String) extends BaseException {
  override val code: today.expresso.common.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.EMAIL_NOT_FOUND
}
