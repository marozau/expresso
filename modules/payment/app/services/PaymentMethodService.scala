package services

import java.time.LocalDate

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import models.daos.PaymentMethodDao
import play.api.libs.json.JsValue
import today.expresso.stream.domain.event.payment._
import today.expresso.stream.domain.model.payment.PaymentMethod.Status.Status
import today.expresso.stream.domain.model.payment.PaymentOption.PaymentOption
import today.expresso.stream.domain.model.payment.PaymentSystem.PaymentSystem

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentMethodService @Inject() (paymentMethodDao: PaymentMethodDao)
                                     (implicit ec: ExecutionContext, system: ActorSystem) {

  val stream = system.eventStream

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
                       details: Option[JsValue]) = {
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
        Future.successful(stream.publish(PaymentMethodAdded(paymentMethod)))
    }
  }

  def removePaymentMethod(userId: Long, paymentMethodId: Long) = {
    paymentMethodDao.remove(userId, paymentMethodId) { paymentMethod =>
      Future.successful(stream.publish(PaymentMethodRemoved(paymentMethod)))
    }
  }

  def updatePaymentMethod(userId: Long,
             paymentMethodId: Long,
             status: Option[Status],
             details: Option[JsValue],
             displayName: Option[String],
             expirationDate: Option[LocalDate],
             isDefault: Option[Boolean]) = {
    paymentMethodDao.update(
      userId,
      paymentMethodId,
      status,
      details,
      displayName,
      expirationDate,
      isDefault
    ) { paymentMethod =>
      Future.successful(stream.publish(PaymentMethodUpdated(paymentMethod)))
    }
  }

  def successPayment(paymentMethodId: Long, date: LocalDate) = {
    paymentMethodDao.successPayment(paymentMethodId, date) { paymentMethod =>
      Future.successful(stream.publish(PaymentMethodSuccess(paymentMethod)))
    }
  }

  def failedPayment(paymentMethodId: Long, date: LocalDate) = {
    paymentMethodDao.failedPayment(paymentMethodId, date) { paymentMethod =>
      Future.successful(stream.publish(PaymentMethodFailed(paymentMethod)))
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
