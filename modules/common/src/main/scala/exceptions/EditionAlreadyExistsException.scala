package exceptions

/**
  * @author im.
  */
case class EditionAlreadyExistsException(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.EDITION_ALREADY_EXISTS
}

object EditionAlreadyExistsException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.EDITION_ALREADY_EXISTS.toString))
      throw EditionAlreadyExistsException(message())
  }
}

