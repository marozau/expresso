package api

import javax.inject.{Inject, Singleton}

import api.UserServiceGrpcImpl.userDtoCast
import grpc.GrpcErrorHandler
import models.User
import org.slf4j.{Logger, LoggerFactory}
import services.UserService
import today.expresso.grpc.user.dto.{LoginInfoDto, UserIdentityDto}
import today.expresso.grpc.user.service.{UserGetByLoginInfoRequest, UserGetByLoginInfoResponse, UserIdentityServiceGrpc}

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class UserIdentityServiceGrpcImpl @Inject()(userService: UserService)(implicit ec: ExecutionContext)
  extends UserIdentityServiceGrpc.UserIdentityService {

  import UserIdentityServiceGrpcImpl._
  import UserServiceGrpcImpl._

  val log: Logger = LoggerFactory.getLogger(classOf[UserServiceGrpcImpl])

  override def userGetByLoginInfo(request: UserGetByLoginInfoRequest) = GrpcErrorHandler {
    log.info(s"userGetByLoginInfo - {}", request)
    require(request.loginInfo.nonEmpty, "loginInfo is empty")

    userService.getByLoginInfo(request.loginInfo.get)
      .map { userOption: Option[models.User] =>
        UserGetByLoginInfoResponse(
          request.header.map(_.copy(token = "")),
          userOption.map(userIdentityDtoCast)
        )
      }
  }
}

object UserIdentityServiceGrpcImpl {
  import UserServiceGrpcImpl._

  implicit def userIdentityDtoCast(user: User): UserIdentityDto = {
    UserIdentityDto(
      Some(LoginInfoDto(user.loginInfo.providerID, user.loginInfo.providerKey)),
      user.id.get,
      user.roles.map(userDtoRoleCast),
      user.status,
      user.locale.getOrElse("")
    )
  }
}
