package today.expresso.common.exceptions

case class PaymentMethodNotFoundException(message: String) extends BaseException {
  override val code: BaseException.ErrorCode.Value = BaseException.ErrorCode.PAYMENT_METHOD_NOT_FOUND
}

object PaymentMethodNotFoundException extends BaseExceptionReflection {
  override def throwException(code: String, message: () => String): Unit = {
    if (code.equals(BaseException.ErrorCode.PAYMENT_METHOD_NOT_FOUND.toString))
      throw PaymentMethodNotFoundException(message())
  }
}


