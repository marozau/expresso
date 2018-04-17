package services

import javax.inject.{Inject, Singleton}
import models.Currency
import models.daos.CurrencyDao

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class CurrencyService @Inject() (currencyDao: CurrencyDao)(implicit ec: ExecutionContext){

  private val currencies: Map[String, Currency] = Await.result(currencyDao.getCurrencyAll(), Duration.Inf).map(c => c.code -> c).toMap

  def getAll() = currencies.values

  def get(code: String) = currencies(code)
}
