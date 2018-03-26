package today.expresso.common.exceptions

/**
  * @author im.
  */
case class UserAlreadyExistsException(message: String) extends BaseException {
  override val code: today.expresso.common.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.USER_NOT_FOUND
}

object UserAlreadyExistsException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.USER_ALREADY_EXISTS.toString))
      throw UserAlreadyExistsException(message())
  }
}