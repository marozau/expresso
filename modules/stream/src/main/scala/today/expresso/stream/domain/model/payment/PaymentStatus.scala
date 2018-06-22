package today.expresso.stream.domain.model.payment

object PaymentStatus extends Enumeration {
  type PaymentStatus = Value

  val SUCCESSFUL = Value
  val DECLINED = Value
  val CANCELLED = Value
}
