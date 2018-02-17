package services

import javax.inject.{Inject, Singleton}

import today.expresso.grpc.user.domain.User

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class UserService @Inject()()(implicit ec: ExecutionContext) {

  // TODO: must just create new user with status pending
  // when user verify subscription it sends event that will be used as user verification email
  def createReader(email: String): Future[User] = ???
}
