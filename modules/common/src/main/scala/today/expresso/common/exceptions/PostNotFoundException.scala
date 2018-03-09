package today.expresso.common.exceptions

/**
  * @author im.
  */
case class PostNotFoundException(message: String) extends BaseException {
  override val code: today.expresso.common.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.POST_NOT_FOUND
}

object PostNotFoundException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.POST_NOT_FOUND.toString))
      throw PostNotFoundException(message())
  }
}
