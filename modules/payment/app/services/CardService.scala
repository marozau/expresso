package services

import java.time.LocalDate

import javax.inject.{Inject, Singleton}
import models.Card
import models.daos.CardDao

import scala.concurrent.ExecutionContext

@Singleton
class CardService @Inject()(cardDao: CardDao)(implicit ec: ExecutionContext) {

  def searchCard(userId: Long, pan: String, expirationDate: LocalDate) = {
    cardDao.getCards(userId, expirationDate)
  }
}
