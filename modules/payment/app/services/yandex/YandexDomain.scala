package services.yandex

object YandexDomain {

  object ConfirmationType extends Enumeration {
    type ConfirmationType = Value

    val redirect = Value
    val external = Value
  }

  case class Amount(value: BigDecimal, currency: String)

  case class Item(description: String, quantity: String, amount: Amount, vatCode: Int)

  case class Receipt(items: Seq[Item], taxSystemCode: Option[Int], phone: Option[String], email: Option[String])

  case class Recipient(gatewayId: String)

  case class PaymentMethod(xtype: String, id: String, saved: Boolean, title: Option[String], phone: Option[String])

  case class Confirmation(xtype: String, enforce: Option[Boolean], returnUrl: Option[String], confirmationUrl: Option[String])

  //https://kassa.yandex.ru/docs/checkout-api/#sozdanie-platezha
  case class DepositRequest(amount: Amount,
                            description: Option[String],
                            receipt: Option[Receipt],
                            recipient: Option[Recipient],
                            paymentToken: Option[String],
                            paymentMethodId: Option[String],
                            confirmation: Option[Confirmation],
                            savePaymentMethod: Option[Boolean],
                            capture: Option[Boolean],
                            clientIp: Option[String],
                            metadata: Option[Map[String, String]])

  case class DepositResponse(id: String,
                             status: String,
                             amount: Amount,
                             description: Option[String],
                             recipient: Option[Recipient],
                             paymentMethod: PaymentMethod,
                             capturedAt: Option[String],
                             createdAt: String,
                             expiresAt: Option[String],
                             confirmation: Option[Confirmation],
                             test: Boolean,
                             refundedAmount: Option[Amount],
                             paid: Boolean,
                             receiptRegistration: Option[String],
                             metadata: Option[Map[String, String]])

  case class Error(xtype: Option[String],
                   id: Option[String],
                   code: Option[String],
                   description: Option[String],
                   parameter: Option[String],
                   retryAfter: Option[String])

  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  implicit val amountReads: Reads[Amount] = (
    (__ \ "value").read[BigDecimal] and
      (__ \ "currency").read[String]
    ) (Amount.apply _)

  implicit val amountWrites: Writes[Amount] = (
    (__ \ "value").write[String] and
      (__ \ "currency").write[String]
    ) (unlift(Amount.unapply))

  implicit val itemReads: Reads[Item] = (
    (__ \ "description").read[String] and
      (__ \ "quantity").read[String] and
      (__ \ "amount").read[Amount] and
      (__ \ "vat_code").read[Int]
    ) (Item.apply _)

  implicit val itemWrites: Writes[Item] = (
    (__ \ "description").write[String] and
      (__ \ "quantity").write[String] and
      (__ \ "amount").write[Amount] and
      (__ \ "vat_code").write[Int]
    ) (Item.apply _)

  implicit val receiptReads: Reads[Receipt] = (
    (__ \ "items").read[Seq[Item]] and
      (__ \ "tax_system_code").readNullable[Int] and
      (__ \ "phone").readNullable[String] and
      (__ \ "email").readNullable[String]
    ) (Receipt.apply _)

  implicit val receiptWrites: Writes[Receipt] = (
    (__ \ "items").write[Seq[Item]] and
      (__ \ "tax_system_code").writeNullable[Int] and
      (__ \ "phone").writeNullable[String] and
      (__ \ "email").writeNullable[String]
    ) (Receipt.apply _)

  implicit val recipientReads: Reads[Recipient] = (JsPath \ "gateway_id").read[String].map(Recipient.apply)

  implicit val recipientWrites: Writes[Recipient] = (recipient: Recipient) => Json.obj("gateway_id" -> recipient.gatewayId)

  implicit val paymentMethodReads: Reads[PaymentMethod] = (
    (__ \ "type").read[String] and
      (__ \ "id").read[String] and
      (__ \ "saved").read[Boolean] and
      (__ \ "title").readNullable[String] and
      (__ \ "email").readNullable[String]
    ) (PaymentMethod.apply _)

  implicit val confirmationReads: Reads[Confirmation] = (
    (__ \ "type").read[String] and
      (__ \ "enforce").readNullable[Boolean] and
      (__ \ "return_url").readNullable[String] and
      (__ \ "confirmation_url").readNullable[String]
    ) (Confirmation.apply _)

  implicit val confirmationWrites: Writes[Confirmation] = (
    (__ \ "type").write[String] and
      (__ \ "enforce").write[Boolean] and
      (__ \ "return_url").writeNullable[String] and
      (__ \ "confirmation_url").writeNullable[String]
    ) (Confirmation.apply _)


  implicit val depositRequestWrites: Writes[DepositRequest] = (
    (__ \ "amount").write[Amount] and
      (__ \ "description").writeNullable[String] and
      (__ \ "receipt").writeNullable[Receipt] and
      (__ \ "recipient").writeNullable[Recipient] and
      (__ \ "payment_token").writeNullable[String] and
      (__ \ "payment_method_id").writeNullable[String] and
      (__ \ "confirmation").writeNullable[Confirmation] and
      (__ \ "save_payment_method").writeNullable[Boolean] and
      (__ \ "capture").writeNullable[Boolean] and
      (__ \ "client_ip").writeNullable[String] and
      (__ \ "metadata").writeNullable[Map[String, String]]
    ) (DepositRequest.apply _)

  implicit val depositResponseReads: Reads[DepositResponse] = (
    (__ \ "id").read[String] and
      (__ \ "status").read[String] and
      (__ \ "amount").read[Amount] and
      (__ \ "description").readNullable[String] and
      (__ \ "recipient").readNullable[Recipient] and
      (__ \ "payment_method").read[PaymentMethod] and
      (__ \ "captured_at").readNullable[String] and
      (__ \ "created_at").read[String] and
      (__ \ "expires_at").readNullable[String] and
      (__ \ "confirmation").readNullable[Confirmation] and
      (__ \ "test").read[Boolean] and
      (__ \ "refunded_amount").read[Amount] and
      (__ \ "paid").read[Boolean] and
      (__ \ "receipt_registration").readNullable[String] and
      (__ \ "metadata").readNullable[Map[String, String]]
    ) (DepositResponse.apply _)


  implicit val errorReads: Reads[DepositResponse] = (
    (__ \ "type").readNullable[String] and
      (__ \ "id").readNullable[String] and
      (__ \ "code").readNullable[Amount] and
      (__ \ "description").readNullable[String] and
      (__ \ "parameter").readNullable[String] and
      (__ \ "retry_after").readNullable[String]
    )(Error.apply _)
}
