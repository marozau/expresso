package services

import java.time.LocalDate

import javax.inject.{Inject, Singleton}
import models.PaymentMethod.PaymentOption.PaymentOption
import models.PaymentMethod.PaymentSystem.PaymentSystem
import models.PaymentMethod.Status.Status
import models.PaymentMethod.{ToPaymentMethodAdded, ToPaymentMethodFailed, ToPaymentMethodRemoved, ToPaymentMethodSuccess, ToPaymentMethodUpdated}
import models.daos.PaymentMethodDao
import play.api.libs.json.JsValue
import today.expresso.stream.ProducerPool

import scala.concurrent.ExecutionContext

@Singleton
class PaymentMethodService @Inject() (paymentMethodDao: PaymentMethodDao)
                                     (implicit ec: ExecutionContext, pp: ProducerPool) {

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
                       details: Option[JsValue]) = pp.transaction { producer =>
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

  def removePaymentMethod(userId: Long, paymentMethodId: Long) = pp.transaction { producer =>
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
             isDefault: Option[Boolean]) = pp.transaction { producer =>
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

  def successPayment(paymentMethodId: Long, date: LocalDate) = pp.transaction { producer =>
    paymentMethodDao.successPayment(paymentMethodId, date) { paymentMethod =>
      producer.send(ToPaymentMethodSuccess(paymentMethod))
    }
  }

  def failedPayment(paymentMethodId: Long, date: LocalDate) = pp.transaction { producer =>
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
