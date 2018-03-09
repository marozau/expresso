package today.expresso.common.exceptions

/**
  * @author im.
  */
case class UserUnverifiedException(message: String) extends BaseException {
  override val code: today.expresso.common.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.USER_UNVERIFIED
}
