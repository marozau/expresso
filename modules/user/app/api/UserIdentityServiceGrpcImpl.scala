package api

import javax.inject.{Inject, Singleton}

import grpc.GrpcErrorHandler
import models.User
import org.slf4j.{Logger, LoggerFactory}
import services.UserIdentityService
import today.expresso.grpc.user.dto.UserIdentityDto
import today.expresso.grpc.user.service.{UserGetByLoginInfoRequest, UserGetByLoginInfoResponse, UserIdentityServiceGrpc}

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class UserIdentityServiceGrpcImpl @Inject()(userIdentityService: UserIdentityService)(implicit ec: ExecutionContext)
  extends UserIdentityServiceGrpc.UserIdentityService {

  import UserIdentityServiceGrpcImpl._
  import UserServiceGrpcImpl._

  val log: Logger = LoggerFactory.getLogger(classOf[UserServiceGrpcImpl])

  override def userGetByLoginInfo(request: UserGetByLoginInfoRequest) = GrpcErrorHandler {
    log.info(s"userGetByLoginInfo - {}", request)

    require(request.loginInfo.nonEmpty, "loginInfo is empty")

    userIdentityService.retrieve(request.loginInfo.get)
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
      user.id,
      user.status,
      user.roles.map(userDtoRoleCast),
      user.locale.getOrElse("")
    )
  }
}
