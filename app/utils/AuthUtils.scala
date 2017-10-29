package utils

import com.mohiva.play.silhouette.api.Authorization
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.{User, UserRole}
import play.api.mvc.Request

import scala.concurrent.Future

/**
  * @author im.
  */
/**
  * Only allows those users that have one of the requested roles
  *
  * @param anyOf sequence of allowed roles
  */
case class WithRole(anyOf: UserRole.Value*) extends Authorization[User, CookieAuthenticator] {
  override def isAuthorized[B](user: User, authenticator: CookieAuthenticator)(implicit request: Request[B]) = Future.successful {
    WithRole.isAuthorized(user, anyOf: _*)
  }
}

object WithRole {
  def isAuthorized(user: User, anyOf: UserRole.Value*): Boolean = {
    anyOf.intersect(user.roles).nonEmpty
  }
}

/**
  * Only allows those users that have every of the selected roles
  *
  * @param allOf sequence of allowed roles
  */
case class WithRoles(allOf: UserRole.Value*) extends Authorization[User, CookieAuthenticator] {
  override def isAuthorized[B](user: User, authenticator: CookieAuthenticator)(implicit request: Request[B]) = Future.successful {
    WithRoles.isAuthorized(user, allOf: _*)
  }
}

object WithRoles {
  def isAuthorized(user: User, allOf: UserRole.Value*): Boolean =
    allOf.intersect(user.roles).size == allOf.size
}


