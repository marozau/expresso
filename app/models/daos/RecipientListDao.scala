package models.daos

import javax.inject.{Inject, Singleton}

import models.RecipientList
import models.api.Repository
import models.components.{RecipientComponent, RecipientListComponent, UserComponent}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class RecipientListDao @Inject()(databaseConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with RecipientComponent with RecipientListComponent with UserComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  implicit def recipientListCast(list: DBRecipientList): RecipientList = RecipientList(list.id, list.userId, list.name, list.default)

  def createList(userId: Long, listName: String): Future[RecipientList] = db.run {
    ((rlists returning rlists) += DBRecipientList(None, userId, listName))
      .map(result => RecipientList(result.id, result.userId, result.name, result.default))
  }

  def getByUserIdDBIO(userId: Long) = {
    rlists.filter(_.userId === userId).result.map(_.map(recipientListCast))
  }

  def getByUserId(userId: Long): Future[Seq[RecipientList]] = db.run(getByUserIdDBIO(userId))
}

