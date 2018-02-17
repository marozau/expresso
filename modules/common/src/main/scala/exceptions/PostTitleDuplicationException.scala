package exceptions

/**
  * @author im.
  */
case class PostTitleDuplicationException(message: String) extends BaseException {
  override val code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.POST_TITLE_DUPLICATION
}

object PostTitleDuplicationException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.POST_TITLE_DUPLICATION.toString))
      throw PostTitleDuplicationException(message())
  }
}
