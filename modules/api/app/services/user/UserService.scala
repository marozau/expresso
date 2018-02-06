package services.user

import java.util.concurrent.ThreadLocalRandom
import javax.inject.{Inject, Singleton}

import models.Credentials
import today.expresso.grpc.Header
import today.expresso.grpc.user.service.{UserGetByIdRequest, UserServiceGrpc}

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class UserService @Inject() (userService: UserServiceGrpc.UserService)(implicit ec: ExecutionContext) {

  def getById(userId: Long)(implicit credentials: Credentials) = {
    userService.userGetById(
      UserGetByIdRequest(
        Some(Header(ThreadLocalRandom.current().nextInt(), "", Some(credentials))),
        userId
      )
    ).map(_.user)
  }
}
