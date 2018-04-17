package models.daos

import java.time.LocalDate

import javax.inject.{Inject, Singleton}
import models.Card
import models.components.{CardComponent, CommonComponent}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.{JdbcProfile, ResultSetConcurrency, ResultSetType}
import today.expresso.common.db.Repository

import scala.concurrent.ExecutionContext

@Singleton
class CardDao @Inject()(databaseConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with CardComponent with CommonComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def getCardsStream() = {
    val query = sql"SELECT * FROM payment_method_get_cards()".as[Card]
      .withStatementParameters(rsType = ResultSetType.ForwardOnly, rsConcurrency = ResultSetConcurrency.ReadOnly, fetchSize = 100)
      .transactionally
    db.stream(query)
  }

  def getCards(userId: Long, expirationDate: LocalDate) = {
    val query = sql"SELECT * FROM payment_method_get_cards(${userId}, ${expirationDate})".as[Card]
    db.run(query)
  }

  def savePan(token: String, pan: String) = {
    val query = sql"SELECT * FROM payment_method_card_save_pan(${token}, ${pan})".as[Unit].head
    db.run(query.transactionally)
  }

  def changePan(token: String, pan: String) = {
    val query = sql"SELECT * FROM payment_method_card_change_pan(${token}, ${pan})".as[Boolean].head
    db.run(query.transactionally)
  }

  def getCard(paymentMethodId: Long) = {
    val query = sql"SELECT * FROM payment_method_card_get(${paymentMethodId})".as[Card].head
    db.run(query)
  }

  def deleteRedundant() = {
    val query = sql"SELECT * FROM payment_method_card_delete_redundant()".as[Boolean].head
    db.run(query.transactionally)
  }
}
