package api

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import exceptions.InvalidEmailException
import grpc.GrpcErrorHandler
import models.User
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.mailer.MailerClient
import services.{AuthTokenService, UserService}
import today.expresso.grpc.user.dto._
import today.expresso.grpc.user.service._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * @author im.
  */

@Singleton
class UserServiceGrpcImpl @Inject()(userService: UserService,
                                    authInfoRepository: AuthInfoRepository,
                                    authTokenService: AuthTokenService,
                                    mailerClient: MailerClient)
                                   (implicit ec: ExecutionContext)
  extends UserServiceGrpc.UserService {

  import UserServiceGrpcImpl._

  val log: Logger = LoggerFactory.getLogger(classOf[UserServiceGrpcImpl])

  override def userGetById(request: UserGetByIdRequest) = GrpcErrorHandler {
    log.info(s"userGetById - {}", request)
    userService.getById(request.userId)
      .map { userOption =>
        UserGetByIdResponse(
          request.header.map(_.copy(token = "")),
          userOption.map(userDtoCast)
        )
      }
  }

  //TODO: validate email address
  //TODO: validate password
  override def userCreate(request: UserCreateRequest) = GrpcErrorHandler {
    log.info(s"userCreate - {}", request)
    val domain = "expresso.today"
    if (!request.email.endsWith(domain)) throw InvalidEmailException("@expresso.today domain is allowed only")

    userService.save(request.email, request.password, None, None).flatMap { user => //TODO: locale and timezone

      authTokenService.create(user.id, 1.day).map { token =>
        //          val url = routes.ActivateAccountController.activate(authToken.id).absoluteURL()
        //TODO: replace by emailService request
        val url = s"http://localhost:9000/activate?token=${token.id}"
        //          mailerClient.send(Email(
        //            subject = "email.sign.up.subject",
        //            from = "email.from",
        //            to = Seq(request.email),
        //            bodyText = Some(s"""Click <a href="$url">here</a> to send the activation email again.""")
        //            //                bodyText = Some(views.txt.emails.signUp(user, url).body),
        //            //                bodyHtml = Some(views.html.emails.signUp(user, url).body)
        //          ))

        UserCreateResponse(
          request.header,
          Some(user)
        )
      }
    }
  }

  override def userVerify(request: UserVerifyRequest) = GrpcErrorHandler {
    log.info(s"{}", request)
    val token = UUID.fromString(request.token)
    userService.verify(1L, token).map { user =>
      UserVerifyResponse(request.header)
    }
  }
}

object UserServiceGrpcImpl {

  implicit def userDtoRoleCast(role: User.Role.Value) = role match {
    case User.Role.USER => UserDto.Role.USER
    case User.Role.READER => UserDto.Role.READER
    case User.Role.MEMBER => UserDto.Role.MEMBER
    case User.Role.WRITER => UserDto.Role.WRITER
    case User.Role.EDITOR => UserDto.Role.EDITOR
    case User.Role.CHIEF_EDITOR => UserDto.Role.CHIEF_EDITOR
    case User.Role.ADMIN => UserDto.Role.ADMIN
    case User.Role.API => UserDto.Role.API
    case role => throw new UnsupportedOperationException("unknown role: " + role)
  }

  implicit def userDtoStatusCast(status: User.Status.Value) = status match {
    case User.Status.NEW => UserDto.Status.NEW
    case User.Status.VERIFIED => UserDto.Status.VERIFIED
    case User.Status.BLOCKED => UserDto.Status.BLOCKED
    case status => throw new UnsupportedOperationException("unknown status: " + status)
  }

  implicit def userDtoCast(user: models.User): UserDto = {
    UserDto(
      user.id,
      user.status,
      user.roles.map(userDtoRoleCast),
      user.locale.getOrElse(""),
      user.timezone.getOrElse(0),
      user.reason.getOrElse(""),
      user.createdTimestamp.toEpochMilli,
    )
  }

  implicit def loginInfoDtoCast(loginInfo: LoginInfoDto): LoginInfo =
    LoginInfo(loginInfo.providerId, loginInfo.providerKey)
}
