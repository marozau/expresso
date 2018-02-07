package exceptions

/**
  * @author im.
  */
//TODO: move BaseException.ErrorCode.INVALID_AUTH_TOKEN to the object and create trait for this
case class InvalidAuthTokenException(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.INVALID_AUTH_TOKEN
}

object InvalidAuthTokenException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.INVALID_AUTH_TOKEN.toString))
      throw InvalidAuthTokenException(message())
  }
}
