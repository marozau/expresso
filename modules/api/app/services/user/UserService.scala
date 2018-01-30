package services.user

import java.util.concurrent.ThreadLocalRandom
import javax.inject.{Inject, Singleton}

import models.Credentials
import today.expresso.grpc.Header
import today.expresso.grpc.user.service.UserGetByIdRequest
import today.expresso.grpc.user.service.UserServiceGrpc.UserServiceStub

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class UserService @Inject() (userServiceStub: UserServiceStub)(implicit ec: ExecutionContext) {

  def getById(userId: Long)(implicit credentials: Credentials) = {
    userServiceStub.userGetById(
      UserGetByIdRequest(
        Some(Header(ThreadLocalRandom.current().nextInt(), "", Some(credentials))),
        userId
      )
    ).map(_.user)
  }
}
