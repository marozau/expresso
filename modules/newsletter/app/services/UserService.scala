package services

import javax.inject.{Inject, Singleton}

import today.expresso.grpc.user.domain.User

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
trait UserService {

  def createReader(email: String): Future[User]
  def getById(userId: Long): Future[User]
}

@Singleton
class UserServiceImpl @Inject()()(implicit ec: ExecutionContext) extends UserService {

  // TODO: must just create new user with status pending
  // when user verify subscription it sends event that will be used as user verification email
  override def createReader(email: String): Future[User] = ???

  override def getById(userId: Long): Future[User] = ???
}
