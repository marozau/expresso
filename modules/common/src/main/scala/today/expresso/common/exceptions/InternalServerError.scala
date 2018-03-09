package today.expresso.common.exceptions

/**
  * @author im.
  */
case class InternalServerError(message: String) extends BaseException {
  override val code: today.expresso.common.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.INTERNAL_SERVER_ERROR
}
