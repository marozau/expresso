package today.expresso.stream.domain.model.payment

object PaymentOption extends Enumeration {
  type PaymentOption = Value
  val BANK_CARD = Value
  val YANDEX_WALLET = Value
  val APPLE_PAY = Value
  val SBERBANK = Value
  val QIWI = Value
  val WEBMONEY = Value
  val CASH = Value
  val MOBILE_BALANCE = Value
  val ALFABANK = Value
}
