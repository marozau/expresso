package services

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

import exceptions.PostNotFoundException
import models.{Edition, Post}
import models.daos.EditionDao

/**
  * @author im.
  */
@Singleton
class EditionService @Inject()(editionDao: EditionDao) {

  def list(newsletterId: Long) = editionDao.listSpec(newsletterId)

  def create(newsletterId: Long) = editionDao.create(newsletterId)

  def getCurrent(newsletterId: Long) = editionDao.getCurrent(newsletterId)

  /**
    * Get newsletter edition post for the specified date or throw exception if none
    *
    * @param newsletterId The newsletter id of interested edition
    * @param date         The date when edition was published
    * @return The edition object
    */
  def getByDate(newsletterId: Long, date: LocalDate) = editionDao.getByDate(newsletterId, date)

  def getById(id: Long) = editionDao.getById(id)

  def update(edition: Edition) = editionDao.update(edition)

  def addPost(editionId: Long, postIds: List[Long]) = editionDao.addPost(editionId, postIds)

  def removePost(editionId: Long, postId: Long) = editionDao.removePost(editionId, postId)

  def moveUpPost(editionId: Long, postId: Long) = editionDao.moveUpPost(editionId, postId)

  def moveDownPost(editionId: Long, postId: Long) = editionDao.moveDownPost(editionId, postId)
}