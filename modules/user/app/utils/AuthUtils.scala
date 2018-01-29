package utils

import com.mohiva.play.silhouette.api.Authorization
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.User
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
case class WithRole(anyOf: User.Role.Value*) extends Authorization[User, JWTAuthenticator] {
  override def isAuthorized[B](user: User, authenticator: JWTAuthenticator)(implicit request: Request[B]) = Future.successful {
    WithRole.isAuthorized(user, anyOf: _*)
  }
}

object WithRole {
  def isAuthorized(user: User, anyOf: User.Role.Value*): Boolean = {
    anyOf.intersect(user.roles).nonEmpty
  }
}

/**
  * Only allows those users that have every of the selected roles
  *
  * @param allOf sequence of allowed roles
  */
case class WithRoles(allOf: User.Role.Value*) extends Authorization[User, JWTAuthenticator] {
  override def isAuthorized[B](user: User, authenticator: JWTAuthenticator)(implicit request: Request[B]) = Future.successful {
    WithRoles.isAuthorized(user, allOf: _*)
  }
}

object WithRoles {
  def isAuthorized(user: User, allOf: User.Role.Value*): Boolean =
    allOf.intersect(user.roles).size == allOf.size
}


