package services

import javax.inject.{Inject, Named, Singleton}
import models.PaymentMethod.PaymentSystem.PaymentSystem
import models.daos.PaymentNotificationDao
import play.api.libs.json.JsValue
import streams.Names
import today.expresso.stream.Producer
import today.expresso.stream.domain.event.payment.PaymentNotification

import scala.concurrent.ExecutionContext

@Singleton
class PaymentNotificationService @Inject()(paymentNotificationDao: PaymentNotificationDao)
                                          (implicit ec: ExecutionContext, @Named(Names.paymentNotifications) producer: Producer) {

  def savePaymentNotification(key: String, userId: Long, paymentSystem: PaymentSystem, data: JsValue) = {
    paymentNotificationDao.saveNotification(key, userId, paymentSystem, data)
  }

  def getPaymentNotification(key: String, userId: Long, paymentSystem: PaymentSystem) = {
    paymentNotificationDao.getNotification(key, userId, paymentSystem)
  }

  def sendPaymentNotification(paymentNotification: PaymentNotification) = Producer.transactionally {
    producer.send(paymentNotification)
  }
}
