package services

import java.util.concurrent.ThreadLocalRandom
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.User
import today.expresso.grpc.Header
import today.expresso.grpc.user.dto.{LoginInfoDto, UserDto}
import today.expresso.grpc.user.service.UserServiceGrpc.UserServiceStub
import today.expresso.grpc.user.service._

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
object UserIdentityService {

  implicit def userCast(user: UserDto): User = {
    User(
      user.userId,
      LoginInfo(user.loginInfo.get.providerId, user.loginInfo.get.providerKey),
      user.roles,
      user.status,
      if (user.locale.isEmpty) None else Some(user.locale))

  }
}

@Singleton
class UserIdentityService @Inject()(userService: UserServiceStub)(implicit ec: ExecutionContext)
  extends IdentityService[User] {

  import UserIdentityService._

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    userService.userGetByLoginInfo(
      UserGetByLoginInfoRequest(
        Some(Header(ThreadLocalRandom.current().nextInt())),
        Some(LoginInfoDto(loginInfo.providerID, loginInfo.providerKey))
      )
    )
      .map(_.user.map(userCast))

  }

//  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
//    Future.successful(Some(
//      User(
//        0L,
//        LoginInfo(CredentialsProvider.ID, "admin@expresso.today"),
//        Seq(UserDto.Role.ADMIN),
//        UserDto.Status.VERIFIED,
//        None)))
//  }
}
