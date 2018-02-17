package services

import javax.inject.{Inject, Singleton}

import models.daos.PostDao
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class PostService @Inject() (postDao: PostDao)(implicit ec: ExecutionContext) {

  def create(userId: Long,
             editionId: Long,
             editionOrder: Int,
             title: String,
             annotation: String,
             body: JsValue,
             options: Option[JsValue]) = {
    postDao.create(userId, editionId, editionOrder, title, annotation, body, options)
  }

  def update(userId: Long,
             postId: Long,
             editionOrder: Option[Int],
             title: Option[String],
             annotation: Option[String],
             body: Option[JsValue],
             options: Option[JsValue]) = {
    postDao.update(userId, postId, editionOrder, title, annotation, body, options)
  }

  def getById(userId: Long, postId: Long) = {
    postDao.getById(userId, postId)
  }
}
