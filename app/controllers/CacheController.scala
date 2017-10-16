package controllers

import javax.inject.{Inject, Singleton}

import play.api.cache.AsyncCacheApi
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class CacheController @Inject()(cc: ControllerComponents, asyncCacheApi: AsyncCacheApi)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def removeAll() = Action.async { implicit request =>
    asyncCacheApi.removeAll()
      .map(_ => Ok("Ok"))
  }

}
