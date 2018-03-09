package today.expresso.common.exceptions

/**
  * @author im.
  */
case class InvalidUserStatusException(message: String) extends BaseException {
  override val code: today.expresso.common.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.INVALID_USER_STATUS
}
