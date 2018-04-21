package models.components

import com.github.tminglei.slickpg.utils.PlainSQLUtils
import models.PaymentMethod
import slick.jdbc.GetResult
import today.expresso.common.db.Repository

trait PaymentMethodComponent {
  this: Repository =>

  import api._

  implicit val paymentOptionTypeMapper = createEnumJdbcType("payment_option", PaymentMethod.PaymentOption)
  implicit val paymentOptionListTypeMapper = createEnumListJdbcType("payment_option", PaymentMethod.PaymentOption)

  implicit val paymentSystemTypeMapper = createEnumJdbcType("payment_system", PaymentMethod.PaymentSystem)
  implicit val paymentSystemListTypeMapper = createEnumListJdbcType("payment_system", PaymentMethod.PaymentSystem)

  implicit val paymentMethodStatusTypeMapper = createEnumJdbcType("payment_method_status", PaymentMethod.Status)
  implicit val paymentMethodStatusListTypeMapper = createEnumListJdbcType("payment_method_status", PaymentMethod.Status)

  implicit val paymentOptionSet = PlainSQLUtils.mkSetParameter[PaymentMethod.PaymentOption.Value]("payment_option")
  implicit val paymentOptionOptionSet = PlainSQLUtils.mkOptionSetParameter[PaymentMethod.PaymentOption.Value]("payment_option")
  implicit val paymentSystemSet = PlainSQLUtils.mkSetParameter[PaymentMethod.PaymentSystem.Value]("payment_system")
  implicit val paymentSystemOptionSet = PlainSQLUtils.mkOptionSetParameter[PaymentMethod.PaymentSystem.Value]("payment_system")
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
