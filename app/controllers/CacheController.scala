package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import models.UserRole
import modules.DefaultEnv
import play.api.cache.AsyncCacheApi
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import utils.WithRole

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class CacheController @Inject()(cc: ControllerComponents,
                                silhouette: Silhouette[DefaultEnv],
                                asyncCacheApi: AsyncCacheApi)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def removeAll() = silhouette.SecuredAction(WithRole(UserRole.ADMIN)).async { implicit request: Request[AnyContent] =>
    asyncCacheApi.removeAll()
      .map(_ => Ok("Ok"))
  }

}
