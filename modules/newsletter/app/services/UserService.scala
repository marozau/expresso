package services

import javax.inject.{Inject, Singleton}
import today.expresso.stream.domain.model.user.User

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
  //TODO: replace by create(u: User)
  // when user verify subscription it sends event that will be used as user verification email
  override def createReader(email: String): Future[User] = ???

  override def getById(userId: Long): Future[User] = ???
}
