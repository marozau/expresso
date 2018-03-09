package today.expresso.common.exceptions

/**
  * @author im.
  */
case class AuthorizationException(message: String) extends BaseException {
  override val code: today.expresso.common.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.AUTHORIZATION
}

object AuthorizationException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.AUTHORIZATION.toString))
      throw AuthorizationException(message())
  }
}

