package repositories

import javax.inject.{Inject, Singleton}

import exceptions.RecipientNotFoundException
import models.Recipient
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class RecipientRepository @Inject() (databaseConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with RecipientComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def create(recipient: Recipient): Future[Recipient] = db.run {
    (recipients returning recipients) += recipient
  }

  def getByIdDBIO(id: Long) = {
    recipients.filter(_.id === id).result
      .map { s =>
        if (s.isEmpty) throw RecipientNotFoundException(Some(id), None, "recipient not found")
        s.head
      }
  }

  def getById(id: Long): Future[Recipient] = db.run(getByIdDBIO(id))

  def getByUserIdDBIO(userId: Long) = {
    recipients.filter(_.userId === userId).result
      .map { s =>
        if (s.isEmpty) throw RecipientNotFoundException(None, Some(userId), s"user recipient not found, userId=$userId") //TODO: userId in exception
        s
      }
  }

  def getByUserId(userId: Long): Future[Seq[Recipient]] = db.run(getByUserIdDBIO(userId))
}
