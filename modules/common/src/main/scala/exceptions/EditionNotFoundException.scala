package exceptions

/**
  * @author im.
  */
case class EditionNotFoundException(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.EDITION_NOT_FOUND
}

object EditionNotFoundException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.EDITION_NOT_FOUND.toString))
      throw EditionNotFoundException(message())
  }
}