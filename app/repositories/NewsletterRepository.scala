package repositories

import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}

import exceptions.NewsletterNotFoundException
import models.{Newsletter, NewsletterAndPosts}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import utils.SeqUtils

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class NewsletterRepository @Inject()(
                                      databaseConfigProvider: DatabaseConfigProvider,
                                      postRepo: PostRepository)(implicit ec: ExecutionContext)
  extends Repository with NewsletterComponent with UserComponent with PostComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  def create(newsletter: Newsletter): Future[Newsletter] = db.run {
    {
      for {
        nl <- (newsletters returning newsletters) += newsletter
        _ <- DBIO.sequence(newsletter.postIds.map(id => postRepo.setNewsletterIdDBIO(newsletter.userId, id, nl.id)))
      } yield nl
    }.transactionally.withPinnedSession
  }

  def updateDBIO(newsletter: Newsletter): DBIOAction[Int, NoStream, Effect.Write] = {
    val q = for (p <- newsletters if p.id === newsletter.id && p.userId === newsletter.userId) yield p
    q.update(newsletter)
      .map { updated =>
        if (updated == 0) throw NewsletterNotFoundException(newsletter.id.get, s"newsletter update failed")
        updated
      }
  }

  def update(newsletter: Newsletter): Future[Int] = db.run(updateDBIO(newsletter))

  def getByIdDBIO(userId: Option[Long], id: Long): DBIOAction[Newsletter, NoStream, Effect.Read] = {
    newsletters.filter(nl => nl.id === id && userId.map(u => nl.userId === u).getOrElse(slick.lifted.LiteralColumn(true))).result
      .map { s =>
        if (s.isEmpty) throw NewsletterNotFoundException(id, "newsletter not found")
        s.head
      }
  }

  def getById(userId: Option[Long], id: Long): Future[Newsletter] = db.run(getByIdDBIO(userId, id))

  def getByUserId(userId: Long): Future[Seq[Newsletter]] = db.run {
    newsletters.filter(_.userId === userId).result
  }

  def getWithPostsById(userId: Option[Long], id: Long): Future[NewsletterAndPosts] = db.run {
    for {
      newsletter <- getByIdDBIO(userId, id)
      posts <- postRepo.getListByIdDBIO(newsletter.postIds)
    } yield
      NewsletterAndPosts(newsletter.id, newsletter.userId, newsletter.url,
        newsletter.title, newsletter.header, newsletter.footer, posts,
        newsletter.options, newsletter.createdTimestamp, newsletter.modifiedTimestamp)
  }

  def updateTitleDBIO(userId: Long, id: Long, title: String) = {
    val q = for (nl <- newsletters if nl.id === id && nl.userId === userId) yield nl.title
    q.update(Some(title))
  }

  def updatePublishTimestampDBIO(userId: Long, id: Long, timestamp: ZonedDateTime) = {
    val q = for (nl <- newsletters if nl.id === id && nl.userId === userId) yield nl.publishTimestamp
    q.update(Some(timestamp))
  }

  def updateTitle(userId: Long, id: Long, title: String): Future[Int] = db.run {
    updateTitleDBIO(userId, id, title)
  }

  def addPost(userId: Long, id: Long, postIds: List[Long]): Future[Int] = db.run {
    getByIdDBIO(Some(userId), id)
      .flatMap { newsletter =>
        updateDBIO(newsletter.copy(postIds = (newsletter.postIds ++ postIds).distinct))
      }
      .transactionally.withPinnedSession
  }

  def removePost(userId: Long, id: Long, postId: Long): Future[Int] = db.run {
    getByIdDBIO(Some(userId), id)
      .flatMap { newsletter =>
        updateDBIO {
          val filtered = newsletter.postIds.filter(idx => idx != postId)
          newsletter.copy(postIds = filtered)
        }
      }
      .flatMap { _ =>
        postRepo.setNewsletterIdDBIO(userId, postId, None)
      }
      .transactionally.withPinnedSession
  }

  def moveUpPost(userId: Long, id: Long, postId: Long): Future[Int] = db.run {
    getByIdDBIO(Some(userId), id)
      .flatMap { newsletter =>
        updateDBIO {
          newsletter.copy(postIds = SeqUtils.moveUp(newsletter.postIds, postId))
        }
      }
      .transactionally
  }

  def moveDownPost(userId: Long, id: Long, postId: Long): Future[Int] = db.run {
    getByIdDBIO(Some(userId), id)
      .flatMap { newsletter =>
        updateDBIO {
          newsletter.copy(postIds = SeqUtils.moveDown(newsletter.postIds, postId))
        }
      }
      .transactionally
  }
}
