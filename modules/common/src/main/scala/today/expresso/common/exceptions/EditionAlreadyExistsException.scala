package today.expresso.common.exceptions

/**
  * @author im.
  */
case class EditionAlreadyExistsException(message: String) extends BaseException {
  override val code: today.expresso.common.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.EDITION_ALREADY_EXISTS
}

object EditionAlreadyExistsException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.EDITION_ALREADY_EXISTS.toString))
      throw EditionAlreadyExistsException(message())
  }
}
