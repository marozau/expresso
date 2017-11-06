package models.daos

import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}

import exceptions.{EditionNotFoundException, NewsletterNotFoundException}
import models.api.Repository
import models.components.{EditionComponent, NewsletterComponent, PostComponent, UserComponent}
import models.{Edition, Newsletter, Post}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import utils.SeqUtils

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class EditionDao @Inject()(
                            databaseConfigProvider: DatabaseConfigProvider,
                            postDao: PostDao
                          )(implicit ec: ExecutionContext)
  extends Repository with EditionComponent with NewsletterComponent with UserComponent with PostComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  implicit def dbEditionCast(e: Edition) = DBEdition(e.id, e.newsletterId, e.title, e.header, e.footer, e.posts.map(_.id.get), e.options, e.publishTimestamp)

  def create(edition: Edition) = db.run {
    {
      for {
        nl <- (editions returning editions) += edition
        //        _ <- DBIO.sequence(newsletter.postIds.map(id => postRepo.setNewsletterIdDBIO(newsletter.userId, id, nl.id)))
      } yield nl
    }.transactionally.withPinnedSession
  }

  def updateDBIO(edition: Edition) = {
    val q = for (p <- editions if p.id === edition.id) yield p
    q.update(edition)
      .map { updated =>
        if (updated == 0) throw NewsletterNotFoundException(edition.id.get, s"edition update failed")
        updated
      }
  }

  def update(edition: Edition): Future[Int] = db.run(updateDBIO(edition))

  def getById(id: Long) = {
    val editionQuery = editions.filter(_.id === id).result.headOption
      .map { editionOption =>
        if (editionOption.isEmpty) throw EditionNotFoundException(id, "getById failed")
        editionOption.get
      }
      .flatMap { edition =>
        postDao.getListByIdDBIO(edition.postIds).map((edition, _))
      }
    db.run(editionQuery)
      .map { case (edition, editionPosts) =>
        Edition(
          edition.id,
          edition.newsletterId,
          edition.title,
          edition.header,
          edition.footer,
          editionPosts,
          edition.options,
          edition.publishTimestamp
        )
      }
  }

  def getByNewsletterId(newsletterId: Long) = {
    val editionQuery = editions.filter(_.newsletterId === newsletterId).result.headOption
      .flatMap { editionOption =>
        editionOption.map(edition => postDao.getListByIdDBIO(edition.postIds).map((editionOption, _)))
          .getOrElse(DBIO.successful((None, List.empty[Post])))
      }
    db.run(editionQuery)
      .map { case (editionOption, editionPosts) =>
        editionOption.map { edition =>
          Edition(
            edition.id,
            edition.newsletterId,
            edition.title,
            edition.header,
            edition.footer,
            editionPosts,
            edition.options,
            edition.publishTimestamp
          )
        }
      }
  }

  def getUnpublished(newsletterId: Long) = {
    val editionQuery = editions
      .filter(edition => edition.newsletterId === newsletterId && edition.publishTimestamp.isEmpty)
      .sortBy(_.publishTimestamp.desc).take(1).result.headOption
    db.run(editionQuery.transactionally)
      .flatMap { dbEditionOption =>
        if (dbEditionOption.isDefined) getById(dbEditionOption.get.id.get).map(Some(_))
        else Future.successful(None)
      }
  }

  def getUnpublishedOrCreate(newsletterId: Long) = {
    val editionAction = {
      val retrieveEdition = editions
        .filter(edition => edition.newsletterId === newsletterId && edition.publishTimestamp.isEmpty)
        .sortBy(_.publishTimestamp.desc).take(1).result.headOption
      val insertEdition = (editions returning editions) += DBEdition(None, newsletterId)
      for {
        editionOption <- retrieveEdition
        dbEdition <- editionOption.map(DBIO.successful).getOrElse(insertEdition)
      } yield dbEdition
    }
    db.run(editionAction.transactionally)
      .flatMap { dbEdition =>
        getById(dbEdition.id.get)
      }
  }

  def updateTitleDBIO(id: Long, title: String) = {
    val q = for (nl <- editions if nl.id === id) yield nl.title
    q.update(Some(title))
  }

  def updatePublishTimestampDBIO(id: Long, timestamp: ZonedDateTime) = {
    val q = for (nl <- editions if nl.id === id) yield nl.publishTimestamp
    q.update(Some(timestamp))
  }

  def updateTitle(id: Long, title: String): Future[Int] = db.run {
    updateTitleDBIO(id, title)
  }

  def addPost(id: Long, postIds: List[Long]) = {
    val updateQuery = for {
      editionPostIds <- editions.filter(_.id === id).map(_.postIds).result.headOption
      update <- editions.filter(_.id === id).map(_.postIds)
        .update(editionPostIds.getOrElse(List.empty) ++ postIds)
    } yield update
    db.run(updateQuery.transactionally)
  }

  def removePost(editionId: Long, postId: Long): Future[Int] = {
    val updateQuery = for {
      editionPostIds <- editions.filter(_.id === editionId).map(_.postIds).result.headOption
      update <- editions.filter(_.id === editionId).map(_.postIds)
        .update(editionPostIds.getOrElse(List.empty).filter(idx => idx != postId))
      _ <- posts.filter(_.id === postId).map(_.editionId).update(None)
    } yield update
    db.run(updateQuery.transactionally)
  }

  def moveUpPost(id: Long, postId: Long): Future[Int] = {
    val updateQuery = for {
      editionPostIds <- editions.filter(_.id === id).map(_.postIds).result.headOption
      update <- editions.filter(_.id === id).map(_.postIds)
        .update(SeqUtils.moveUp(editionPostIds.getOrElse(List.empty), postId))
    } yield update
    db.run(updateQuery.transactionally)
  }

  def moveDownPost(id: Long, postId: Long): Future[Int] = {
    val updateQuery = for {
      editionPostIds <- editions.filter(_.id === id).map(_.postIds).result.headOption
      update <- editions.filter(_.id === id).map(_.postIds)
        .update(SeqUtils.moveDown(editionPostIds.getOrElse(List.empty), postId))
    } yield update
    db.run(updateQuery.transactionally)
  }
}
