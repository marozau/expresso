package services

import java.util.concurrent.ThreadLocalRandom
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import models.User
import play.api.cache.AsyncCacheApi
import today.expresso.grpc.Header
import today.expresso.grpc.user.dto.{LoginInfoDto, UserIdentityDto}
import today.expresso.grpc.user.service.UserIdentityServiceGrpc.UserIdentityServiceStub
import today.expresso.grpc.user.service._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * @author im.
  */
object UserIdentityService {

  implicit def userCast(user: UserIdentityDto): User = {
    User(
      user.id,
      LoginInfo(user.loginInfo.get.providerId, user.loginInfo.get.providerKey),
      user.roles,
      user.status,
      if (user.locale.isEmpty) None else Some(user.locale))

  }
}

@Singleton
class UserIdentityService @Inject()(
                                     userService: UserIdentityServiceStub,
                                     cache: AsyncCacheApi
                                   )(implicit ec: ExecutionContext)
  extends IdentityService[User] {

  import UserIdentityService._

  //TODO: redis cache

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
    cache.getOrElseUpdate(s"loginInfo:${loginInfo.providerID}:${loginInfo.providerKey}", 5.minutes) {
      userService.userGetByLoginInfo(
        UserGetByLoginInfoRequest(
          Some(Header(ThreadLocalRandom.current().nextInt())),
          Some(LoginInfoDto(loginInfo.providerID, loginInfo.providerKey))
        )
      )
    }
      .map(_.user.map(userCast))
}
