package models.daos

import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}

import exceptions.{EditionNotFoundException, NewsletterNotFoundException}
import models.api.Repository
import models.components.{EditionComponent, NewsletterComponent, PostComponent, UserComponent}
import models.{Edition, Newsletter, Post}
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
                            postRepo: PostDao
                          )(implicit ec: ExecutionContext)
  extends Repository with EditionComponent with NewsletterComponent with UserComponent with PostComponent {

  protected val dbConfig: DatabaseConfig[JdbcProfile] = databaseConfigProvider.get[JdbcProfile]

  import api._
  import dbConfig._

  implicit def dbEditionCast(e: Edition) = DBEdition(e.id, e.newsletter.id.get, e.title, e.header, e.footer, e.posts.map(_.id.get), e.options, e.publishTimestamp)

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
    val editionQuery = for {
      edition <- editions.filter(_.id === id)
      newsletter <- newsletters.filter(_.id === edition.newsletterId)
      user <- users.filter(_.id === newsletter.userId)
      editionPosts <- posts.filter(_.id === edition.postIds.any)
    } yield (edition, newsletter, user, editionPosts)
    db.run(editionQuery.result)
      .map { (seq: Seq[(DBEdition, DBNewsletter, DBUser, Post)]) =>
        val (editionOption, newsletterOption, userOption, editionPosts) = (seq.map(_._1).headOption, seq.map(_._2).headOption, seq.map(_._3).headOption, seq.map(_._4))
        if (editionOption.isEmpty) throw EditionNotFoundException(id, "getById failed")
        val edition = editionOption.get
        val newsletter = newsletterOption.get
        val user = userOption.get
        Edition(
          edition.id,
          Newsletter(newsletter.id, newsletter.userId, user.email, newsletter.name, newsletter.options),
          edition.title,
          edition.header,
          edition.footer,
          editionPosts.toList,
          edition.options,
          edition.publishTimestamp
        )
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
        edition <- DBIO.from(getById(dbEdition.id.get))
      } yield edition
    }
    db.run(editionAction.transactionally)
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
      editionsPostIds <- editions.filter(_.id === id).map(_.postIds).result.headOption
      update <- editions.filter(_.id === id).map(_.postIds)
        .update(editionsPostIds.getOrElse(List.empty) ++ postIds)
    } yield update
    db.run(updateQuery.transactionally)
  }

  def removePost(editionId: Long, postId: Long): Future[Int] = {
    val updateQuery = for {
      editionsPostIds <- editions.filter(_.id === editionId).map(_.postIds).result.headOption
      update <- editions.filter(_.id === editionId).map(_.postIds)
        .update(editionsPostIds.getOrElse(List.empty).filter(idx => idx != postId))
      _ <- posts.filter(_.id === postId).map(_.editionId).update(None)
    } yield update
    db.run(updateQuery.transactionally)
  }

  def moveUpPost(id: Long, postId: Long): Future[Int] = {
    val updateQuery = for {
      editionsPostIds <- editions.filter(_.id === id).map(_.postIds).result.headOption
      update <- editions.filter(_.id === id).map(_.postIds)
        .update(SeqUtils.moveUp(editionsPostIds.getOrElse(List.empty), postId))
    } yield update
    db.run(updateQuery.transactionally)
  }

  def moveDownPost(id: Long, postId: Long): Future[Int] = {
    val updateQuery = for {
      editionsPostIds <- editions.filter(_.id === id).map(_.postIds).result.headOption
      update <- editions.filter(_.id === id).map(_.postIds)
        .update(SeqUtils.moveDown(editionsPostIds.getOrElse(List.empty), postId))
    } yield update
    db.run(updateQuery.transactionally)
  }
}
