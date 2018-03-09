package today.expresso.common.exceptions

/**
  * @author im.
  */
case class UserNotFoundException(message: String) extends BaseException {
  override val code: today.expresso.common.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.USER_NOT_FOUND
}

object UserNotFoundException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.USER_NOT_FOUND.toString))
      throw UserNotFoundException(message())
  }
}

