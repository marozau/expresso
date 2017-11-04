package services

import javax.inject.{Inject, Singleton}

import models.Edition
import models.daos.EditionDao

/**
  * @author im.
  */
@Singleton
class EditionService @Inject()(editionDao: EditionDao) {

  def getById(id: Long) = editionDao.getById(id)

  def update(edition: Edition) = editionDao.update(edition)

  def getUnpublishedOrCreate(newsletterId: Long) = editionDao.getUnpublishedOrCreate(newsletterId)

  def addPost(editionId: Long, postIds: List[Long]) = editionDao.addPost(editionId, postIds)

  def removePost(editionId: Long, postId: Long) = editionDao.removePost(editionId, postId)

  def moveUpPost(editionId: Long, postId: Long) = editionDao.moveUpPost(editionId, postId)

  def moveDownPost(editionId: Long, postId: Long) = editionDao.moveDownPost(editionId, postId)

}
