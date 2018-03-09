package models.daos

import java.time.{ZoneOffset, ZonedDateTime}
import javax.inject.{Inject, Singleton}

import today.expresso.common.db.Repository
import today.expresso.common.exceptions.{EditionNotFoundException, InvalidCampaignStatusException, PostNotFoundException}
import models.Post
import models.components.{EditionComponent, NewsletterComponent, PostComponent}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
object PostDao {
  val initTimestamp: ZonedDateTime = ZonedDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
}

@Singleton
class PostDao @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with PostComponent with EditionComponent with NewsletterComponent {

  import PostDao._

  protected val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def create(post: Post): Future[Post] = db.run {
    (posts returning posts) += post
  }

  def updateDBIO(post: Post): DBIOAction[Int, NoStream, Effect.Write] = {
    val q = for (p <- posts if p.id === post.id && p.userId === post.userId) yield p
    q.update(post)
      .map { updated =>
        if (updated == 0) throw PostNotFoundException(post.id.get, s"post update failed")
        updated
      }
  }

  def updateDBIO(posts: List[Post]): DBIOAction[List[Int], NoStream, Effect.Write] = {
    DBIO.sequence(posts.map(updateDBIO))
  }

  def update(post: Post): Future[Int] = db.run(updateDBIO(post))

  def update(posts: List[Post]): Future[List[Int]] = db.run(updateDBIO(posts).transactionally)

  def getByIdDBIO(id: Long): DBIOAction[Post, NoStream, Effect.Read] = {
    posts.filter(_.id === id).result.map {
      p =>
        if (p.isEmpty) throw PostNotFoundException(id, s"get post failed")
        p.head
    }
  }

  def getById(id: Long): Future[Post] = db.run(getByIdDBIO(id))

  def getListByIdDBIO(ids: List[Long]): DBIOAction[List[Post], NoStream, Effect.Read] = {
    DBIO.sequence(ids.map(id => getByIdDBIO(id)))
  }

  def getListById(ids: List[Long]): Future[List[Post]] = db.run(getListByIdDBIO(ids))

  //TODO: replace List[Post] by Seq[Post]
  def getList(userId: Long, drop: Int, take: Int): Future[List[Post]] = db.run {
    posts.filter(_.userId === userId)
      .sortBy(_.modifiedTimestamp.desc)
      .drop(drop).take(take)
      .result.map(_.toList)
  }

  def setEditionIdDBIO(postId: Long, editionId: Option[Long]) = {
    posts.filter(_.id === postId).map(_.editionId)
      .update(editionId)
      .map { res =>
        if (res == 0) throw PostNotFoundException(postId, "setEditionIdDBIO failed")
        res
      }
  }

  def setNewsletterId(postId: Long, newsletterId: Option[Long]): Future[Int] = db.run {
    setEditionIdDBIO(postId, newsletterId)
  }
}
