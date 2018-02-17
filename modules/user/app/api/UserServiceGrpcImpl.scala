package api

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.LoginInfo
import grpc.GrpcErrorHandler
import models.User
import org.slf4j.{Logger, LoggerFactory}
import services.UserService
import today.expresso.grpc.user._
import today.expresso.grpc.user.service._

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */

@Singleton
class UserServiceGrpcImpl @Inject()(userService: UserService)(implicit ec: ExecutionContext)
  extends UserServiceGrpc.UserService {

  import UserServiceGrpcImpl._

  val log: Logger = LoggerFactory.getLogger(classOf[UserServiceGrpcImpl])

  override def userGetById(rq: UserGetByIdRequest) = GrpcErrorHandler {
    log.info(s"userGetById - {}", rq)
    userService.getById(rq.userId)
      .map { userOption =>
        UserGetByIdResponse(
          rq.header.map(_.copy(token = "")),
          userOption.map(userDtoCast)
        )
      }
  }

  override def userCreate(rq: UserCreateRequest) = GrpcErrorHandler {
    log.info(s"userCreate - {}", rq)
    userService.save(rq.email, rq.password, None, None).map { user => //TODO: locale and timezone
      UserCreateResponse(
        rq.header,
        Some(user)
      )
    }
  }

  override def userVerify(rq: UserVerifyRequest) = GrpcErrorHandler {
    log.info(s"userVerify - {}", rq)
    val token = UUID.fromString(rq.token)
    userService.verify(1L, token).map { user =>
      UserVerifyResponse(rq.header)
    }
  }

  override def readerCreate(rq: ReaderCreateRequest) = GrpcErrorHandler {
    log.info(s"userCreate - {}", rq)
    userService.createReader(rq.eamil, if (rq.locale.isEmpty) None else Some(rq.locale)).map{ user =>
      ReaderCreateResponse(rq.header, Some(user))
    }
  }
}

object UserServiceGrpcImpl {

  implicit def userDtoRoleCast(role: User.Role.Value) = role match {
    case User.Role.USER => domain.User.Role.USER
    case User.Role.READER => domain.User.Role.READER
    case User.Role.MEMBER => domain.User.Role.MEMBER
    case User.Role.WRITER => domain.User.Role.WRITER
    case User.Role.EDITOR => domain.User.Role.EDITOR
    case User.Role.CHIEF_EDITOR => domain.User.Role.CHIEF_EDITOR
    case User.Role.ADMIN => domain.User.Role.ADMIN
    case User.Role.API => domain.User.Role.API
    case role => throw new UnsupportedOperationException("unknown role: " + role)
  }

  implicit def userDtoStatusCast(status: User.Status.Value) = status match {
    case User.Status.NEW => domain.User.Status.NEW
    case User.Status.VERIFIED => domain.User.Status.VERIFIED
    case User.Status.BLOCKED => domain.User.Status.BLOCKED
    case status => throw new UnsupportedOperationException("unknown status: " + status)
  }

  implicit def userDtoCast(user: models.User): domain.User = {
    domain.User(
      user.id,
      user.email,
      user.status,
      user.roles.map(userDtoRoleCast),
      user.locale.getOrElse(""),
      user.timezone.getOrElse(0),
      user.reason.getOrElse(""),
      user.createdTimestamp.toEpochMilli,
    )
  }

  implicit def loginInfoDtoCast(loginInfo: domain.LoginInfo): LoginInfo =
    LoginInfo(loginInfo.providerId, loginInfo.providerKey)
}
