package services

import java.time.LocalDate

import javax.inject.{Inject, Named, Singleton}
import models.PaymentMethod.PaymentOption.PaymentOption
import models.PaymentMethod.PaymentSystem.PaymentSystem
import models.PaymentMethod.Status.Status
import models.PaymentMethod.{ToPaymentMethodAdded, ToPaymentMethodFailed, ToPaymentMethodRemoved, ToPaymentMethodSuccess, ToPaymentMethodUpdated}
import models.daos.PaymentMethodDao
import play.api.libs.json.JsValue
import streams.Names
import today.expresso.stream.Producer

import scala.concurrent.ExecutionContext

@Singleton
class PaymentMethodService @Inject() (paymentMethodDao: PaymentMethodDao)
                                     (implicit ec: ExecutionContext, @Named(Names.paymentMethods) producer: Producer) {

  def addPaymentMethod(userId: Long,
                       paymentOption: PaymentOption,
                       paymentSystem: PaymentSystem,
                       paymentMethodStatus: Status,
                       expirationDate: Option[LocalDate],
                       displayName: Option[String],
                       isDefault: Boolean,
                       firstPaymentDate: Option[LocalDate],
                       lastPaymentDate: Option[LocalDate],
                       lastFailedDate: Option[LocalDate],
                       details: Option[JsValue]) = Producer.transactionally {
    paymentMethodDao.add(
      userId,
      paymentOption,
      paymentSystem,
      paymentMethodStatus,
      expirationDate,
      displayName,
      isDefault,
      firstPaymentDate,
      lastPaymentDate,
      lastFailedDate,
      details) { paymentMethod =>
        producer.send(ToPaymentMethodAdded(paymentMethod))
    }
  }

  def removePaymentMethod(userId: Long, paymentMethodId: Long) = Producer.transactionally{
    paymentMethodDao.remove(userId, paymentMethodId) { paymentMethod =>
      producer.send(ToPaymentMethodRemoved(paymentMethod))
    }
  }

  def updatePaymentMethod(userId: Long,
             paymentMethodId: Long,
             status: Option[Status],
             details: Option[JsValue],
             displayName: Option[String],
             expirationDate: Option[LocalDate],
             isDefault: Option[Boolean]) = Producer.transactionally{
    paymentMethodDao.update(
      userId,
      paymentMethodId,
      status,
      details,
      displayName,
      expirationDate,
      isDefault
    ) { paymentMethod =>
      producer.send(ToPaymentMethodUpdated(paymentMethod))
    }
  }

  def successPayment(paymentMethodId: Long, date: LocalDate) = Producer.transactionally {
    paymentMethodDao.successPayment(paymentMethodId, date) { paymentMethod =>
      producer.send(ToPaymentMethodSuccess(paymentMethod))
    }
  }

  def failedPayment(paymentMethodId: Long, date: LocalDate) = Producer.transactionally {
    paymentMethodDao.failedPayment(paymentMethodId, date) { paymentMethod =>
      producer.send(ToPaymentMethodFailed(paymentMethod))
    }
  }

  def getPaymentMethod(userId: Long, paymentMethodId: Long) = {
    paymentMethodDao.getByUserIdAndPaymentMethodId(userId, paymentMethodId)
  }

  def getPaymentMethods(userId: Long) = {
    paymentMethodDao.getByUser(userId)
  }

  def search(userId: Long,
             paymentOption: Option[PaymentOption],
             paymentSystem: Option[PaymentSystem],
             details: Option[JsValue]) = {
    paymentMethodDao.search(userId, paymentOption, paymentSystem, details, false, 1)
      .map(_.headOption)
  }

  def searchAll(userId: Long,
                paymentOption: Option[PaymentOption],
                paymentSystem: Option[PaymentSystem],
                details: Option[JsValue],
                successfulOnly: Boolean) = {
    paymentMethodDao.search(userId, paymentOption, paymentSystem, details, successfulOnly, 1)
  }
}
