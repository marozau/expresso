package repositories

import java.time.{ZoneOffset, ZonedDateTime}
import javax.inject.{Inject, Singleton}

import exceptions.PostNotFoundException
import models.Post
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
object PostRepository {
  val initTimestamp: ZonedDateTime = ZonedDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
}

@Singleton
class PostRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends Repository with PostComponent with UserComponent with NewsletterComponent {

  import PostRepository._

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

  def getUnpublished(userId: Long): Future[List[Post]] = db.run {
    newsletters
      .filter(_.publishTimestamp.nonEmpty)
      .sortBy(_.publishTimestamp.desc).take(1).result.headOption
      .flatMap { nl =>
        val publishTimestamp = nl.fold(initTimestamp)(_.publishTimestamp.get.minusDays(1))
        posts
          .filter(p => p.userId === userId && p.createdTimestamp >= publishTimestamp && p.newsletterId.isEmpty)
          .sortBy(_.createdTimestamp)
          .result.map(_.toList)
      }
  }

  def setNewsletterIdDBIO(userId: Long, postId: Long, newsletterId: Option[Long]) = {
    val q = for (p <- posts if p.id === postId && p.userId === userId) yield p.newsletterId
    q.update(newsletterId)
  }

  def setNewsletterId(userId: Long, postId: Long, newsletterId: Option[Long]): Future[Int] = db.run {
    setNewsletterIdDBIO(userId, postId, newsletterId)
  }
}
