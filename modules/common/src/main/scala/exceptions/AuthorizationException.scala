package exceptions

/**
  * @author im.
  */
case class AuthorizationException(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.AUTHORIZATION
}

object AuthorizationException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.AUTHORIZATION.toString))
      throw AuthorizationException(message())
  }
}

