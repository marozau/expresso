package models.components

import com.github.tminglei.slickpg.utils.PlainSQLUtils
import slick.jdbc.GetResult
import today.expresso.common.db.Repository
import today.expresso.stream.domain.model.payment.{PaymentMethod, PaymentOption, PaymentSystem}

trait PaymentMethodComponent {
  this: Repository =>

  import api._

  implicit val paymentOptionTypeMapper = createEnumJdbcType("payment_option", PaymentOption)
  implicit val paymentOptionListTypeMapper = createEnumListJdbcType("payment_option", PaymentOption)

  implicit val paymentSystemTypeMapper = createEnumJdbcType("payment_system", PaymentSystem)
  implicit val paymentSystemListTypeMapper = createEnumListJdbcType("payment_system", PaymentSystem)

  implicit val paymentMethodStatusTypeMapper = createEnumJdbcType("payment_method_status", PaymentMethod.Status)
  implicit val paymentMethodStatusListTypeMapper = createEnumListJdbcType("payment_method_status", PaymentMethod.Status)

  implicit val paymentOptionSet = PlainSQLUtils.mkSetParameter[PaymentOption.Value]("payment_option")
  implicit val paymentOptionOptionSet = PlainSQLUtils.mkOptionSetParameter[PaymentOption.Value]("payment_option")
  implicit val paymentSystemSet = PlainSQLUtils.mkSetParameter[PaymentSystem.Value]("payment_system")
  implicit val paymentSystemOptionSet = PlainSQLUtils.mkOptionSetParameter[PaymentSystem.Value]("payment_system")
  implicit val paymentMethodStatusSet = PlainSQLUtils.mkSetParameter[PaymentMethod.Status.Value]("payment_method_status")
  implicit val paymentMethodStatusOptionSet = PlainSQLUtils.mkOptionSetParameter[PaymentMethod.Status.Value]("payment_method_status")

  implicit val getResultPaymentMethod: GetResult[PaymentMethod] = GetResult { r =>
    PaymentMethod(
      r.nextLong(),
      r.nextLong(),
      paymentOptionTypeMapper.getValue(r.rs, r.skip.currentPos),
      paymentSystemTypeMapper.getValue(r.rs, r.skip.currentPos),
      paymentMethodStatusTypeMapper.getValue(r.rs, r.skip.currentPos),
      r.nextLocalDateOption(),
      r.nextStringOption(),
      r.nextBoolean(),
      r.nextBoolean(),
      r.nextLocalDate(),
      r.nextLocalDateOption(),
      r.nextLocalDateOption(),
      r.nextLocalDateOption(),
      r.nextJsonOption()
    )
  }
}
