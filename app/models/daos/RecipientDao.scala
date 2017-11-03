package models.daos

import javax.inject.{Inject, Singleton}

import models.api.Repository
import models.components.{RecipientComponent, RecipientListComponent, UserComponent}
import models.{Recipient, Recipients}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class RecipientDao @Inject()(databaseConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with RecipientComponent with RecipientListComponent with UserComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def getByListId(userId: Long, listId: Long) = {
    val recipientListAction = rlists.filter(r => r.userId === userId && r.id === listId).result.headOption
    val joinAction = (recipients.filter(_.listId === listId) joinLeft users on (_.userId === _.id)).result

    val q = for {
      recipientListOption <- recipientListAction
      join <- recipientListOption.map(_ => joinAction).getOrElse(DBIO.successful(Seq.empty))
    } yield {
      recipientListOption
        .map { list =>
          val recipients = join.flatMap { case (recipient, userOption) =>
            userOption.map(u => Recipient(u.id.get, u.email, u.status, recipient.status))
          }
          Recipients(list.id, list.userId, list.name, list.default, recipients)
        }
    }

    db.run(q)
  }

  def add(userId: Long, listId: Long, recipient: Recipient): Future[Int] = db.run {
    recipients += DBRecipient(listId, userId, recipient.rstatus)
  }


}
