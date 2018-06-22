package today.expresso.stream.domain.model.payment

object PaymentType extends Enumeration {
  type PaymentType = Value

  val DEPOSIT = Value
  val WITHDRAWAL = Value
  val REFUND = Value
  val AUTH = Value
  val SETTLE = Value
  val VOID = Value
}
