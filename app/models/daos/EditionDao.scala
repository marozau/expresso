package models.daos

import java.net.URL
import java.time.LocalDate
import javax.inject.{Inject, Singleton}

import exceptions.{EditionNotFoundException, NewsletterNotFoundException}
import models.api.Repository
import models.components.{EditionComponent, NewsletterComponent, PostComponent, UserComponent}
import models.{Edition, EditionSpec, Newsletter, Post}
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Lang
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import utils.SeqUtils

import scala.collection.immutable
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

  implicit def dbEditionCast(e: Edition) =
    DBEdition(e.id, e.newsletter.id.get, e.date, e.url.map(_.toString), e.title, e.header, e.footer, e.posts.map(_.id.get), e.options)

  def editionCast(newsletter: DBNewsletter, edition: DBEdition, editionPosts: immutable.Seq[Post]) =
    Edition(
      edition.id,
      Newsletter(
        newsletter.id,
        newsletter.userId,
        newsletter.name,
        newsletter.nameUrl,
        newsletter.email,
        Lang(newsletter.locale),
        newsletter.logoUrl.map(new URL(_)),
        newsletter.options),
      edition.date,
      edition.url.map(new URL(_)),
      edition.title,
      edition.header,
      edition.footer,
      editionPosts.toList,
      edition.options
    )

  implicit def editionSpecCast(e: DBEdition) = EditionSpec(e.id.get, e.date, e.title)

  def create(newsletterId: Long) = {
    val insertEdition = (editions returning editions) += DBEdition(None, newsletterId, LocalDate.now().plusDays(1))
    db.run(insertEdition.transactionally)
      .flatMap { dbEdition =>
        getById(dbEdition.id.get)
      }
  }

  def updateDBIO(edition: Edition) = {
    val q = for (p <- editions if p.id === edition.id) yield p
    q.update(edition)
      .map { updated =>
        if (updated == 0) throw EditionNotFoundException(s"edition update failed, '${edition.id}' not found")
        updated
      }
  }

  def update(edition: Edition): Future[Int] = db.run(updateDBIO(edition))

  private def getByIdDBIO(id: Long) = {
    editions.filter(_.id === id).result.headOption
      .map { editionOption =>
        if (editionOption.isEmpty) throw EditionNotFoundException(s"getById failed, edition with id=$id not found")
        editionOption.get
      }
      .flatMap { edition =>
        newsletters.filter(_.id === edition.newsletterId).result.headOption
          .map { newsletterOption =>
            if (newsletterOption.isEmpty) throw NewsletterNotFoundException(s"getById failed, 'newsletter ${edition.newsletterId}' not found")
            (newsletterOption.get, edition)
          }
      }
      .flatMap { case (newsletter, edition) =>
        postDao.getListByIdDBIO(edition.postIds).map((newsletter, edition, _))
      }
  }

  def getById(id: Long) = {
    db.run(getByIdDBIO(id)).map(result => (editionCast _).tupled(result))
  }

  def getByDate(newsletterId: Long, date: LocalDate) = {
    val query = editions.filter(edition => edition.newsletterId === newsletterId && edition.date === date).result.headOption
      .flatMap { editionOption =>
        if (editionOption.isEmpty) throw EditionNotFoundException(s"getByDate failed, edition for date=$date not found")
        getByIdDBIO(editionOption.get.id.get)
      }
    db.run(query).map(result => (editionCast _).tupled(result))
  }

  //
  //  //FIXME: filter returns all newsletter editions and it is undefined which result.headOption will return
  //  def getByNewsletterId(newsletterId: Long) = {
  //    val editionQuery = editions.filter(_.newsletterId === newsletterId).result.headOption
  //      .flatMap { editionOption =>
  //        editionOption.map(edition => postDao.getListByIdDBIO(edition.postIds).map((editionOption, _)))
  //          .getOrElse(DBIO.successful((None, List.empty[Post])))
  //      }
  //    db.run(editionQuery)
  //      .map { case (editionOption, editionPosts) =>
  //        editionOption.map { edition =>
  //          Edition(
  //            edition.id,
  //            edition.newsletterId,
  //            edition.title,
  //            edition.header,
  //            edition.footer,
  //            editionPosts,
  //            edition.options,
  //            edition.date
  //          )
  //        }
  //      }
  //  }

  def listSpec(newsletterId: Long): Future[Seq[EditionSpec]] = db.run {
    editions.filter(_.newsletterId === newsletterId)
      .sortBy(_.date.desc)
      .result
      .map { editions =>
        editions.map(editionSpecCast)
      }
  }

  def getCurrent(newsletterId: Long) = {
    val query = editions.filter(_.newsletterId === newsletterId).sortBy(_.date.desc).result.headOption
      .flatMap { editionOption =>
        if (editionOption.isEmpty) throw EditionNotFoundException(s"getCurrent failed, '$newsletterId' not found")
        getByIdDBIO(editionOption.get.id.get)
      }
    db.run(query).map(result => (editionCast _).tupled(result))
  }

  def getUnpublished(newsletterId: Long) = {
    val editionQuery = editions
      .filter(edition => edition.newsletterId === newsletterId)
      .sortBy(_.date.desc).take(1).result.headOption
    db.run(editionQuery.transactionally)
      .flatMap { dbEditionOption =>
        if (dbEditionOption.isDefined) getById(dbEditionOption.get.id.get).map(Some(_))
        else Future.successful(None)
      }
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
