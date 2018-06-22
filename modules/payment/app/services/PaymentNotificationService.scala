package services

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import models.daos.PaymentNotificationDao
import play.api.libs.json.JsValue
import today.expresso.stream.domain.event.payment.PaymentNotification
import today.expresso.stream.domain.model.payment.PaymentSystem.PaymentSystem

import scala.concurrent.ExecutionContext

@Singleton
class PaymentNotificationService @Inject()(paymentNotificationDao: PaymentNotificationDao)
                                          (implicit ec: ExecutionContext, system: ActorSystem) {

  val stream = system.eventStream

  def savePaymentNotification(key: String, userId: Long, system: PaymentSystem, data: JsValue) = {
    paymentNotificationDao.saveNotification(key, userId, system, data)
  }

  def getPaymentNotification(key: String, userId: Long, system: PaymentSystem) = {
    paymentNotificationDao.getNotification(key, userId, system)
  }

  def sendPaymentNotification(paymentNotification: PaymentNotification) = {
    stream.publish(paymentNotification)
  }
}
