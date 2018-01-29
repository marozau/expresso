package api

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.api.{LoginInfo, util}
import grpc.GrpcErrorHandler
import models.daos.PasswordInfoDao
import org.slf4j.{Logger, LoggerFactory}
import today.expresso.grpc.user.dto._
import today.expresso.grpc.user.service._

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
//TODO:
//1. Token header authorisation

@Singleton
class PasswordInfoServiceGrpcImpl @Inject()(passwordInfoDao: PasswordInfoDao)(implicit ec: ExecutionContext)
  extends PasswordInfoServiceGrpc.PasswordInfoService {

  import PasswordInfoServiceGrpcImpl._

  val log: Logger = LoggerFactory.getLogger(classOf[UserServiceGrpcImpl])

  override def passwordInfoFind(request: PasswordInfoFindRequest) = GrpcErrorHandler {
    log.info(s"passwordInfoFind - {}", request)
    require(request.loginInfo.nonEmpty, "loginInfo is empty")

    passwordInfoDao.find(request.loginInfo.get)
      .map { passwordInfo =>
        PasswordInfoFindResponse(
          request.header.map(_.copy(token = "")),
          passwordInfo.map(passwordInfoCast)
        )
      }
  }

  override def passwordInfoAdd(request: PasswordInfoAddRequest) = GrpcErrorHandler {
    log.info(s"passwordInfoAdd - {}", request)
    require(request.loginInfo.nonEmpty, "loginInfo is empty")
    require(request.passwordInfo.nonEmpty, "passwordInfo is empty")

    passwordInfoDao.add(request.loginInfo.get, request.passwordInfo.get)
      .map { passwordInfo =>
        PasswordInfoAddResponse(
          request.header.map(_.copy(token = "")),
          Some(passwordInfo)
        )
      }
  }

  override def passwordInfoUpdate(request: PasswordInfoUpdateRequest) = GrpcErrorHandler {
    log.info(s"passwordInfoUpdate - {}", request)
    require(request.loginInfo.nonEmpty, "loginInfo is empty")
    require(request.passwordInfo.nonEmpty, "passwordInfo is empty")

    passwordInfoDao.update(request.loginInfo.get, request.passwordInfo.get)
      .map { passwordInfo =>
        PasswordInfoUpdateResponse(
          request.header.map(_.copy(token = "")),
          Some(passwordInfo)
        )
      }
  }

  override def passwordInfoSave(request: PasswordInfoSaveRequest) = GrpcErrorHandler {
    log.info(s"passwordInfoSave - {}", request)
    require(request.loginInfo.nonEmpty, "loginInfo is empty")
    require(request.passwordInfo.nonEmpty, "passwordInfo is  empty")

    passwordInfoDao.save(request.loginInfo.get, request.passwordInfo.get)
      .map { passwordInfo =>
        PasswordInfoSaveResponse(
          request.header.map(_.copy(token = "")),
          Some(passwordInfo)
        )
      }
  }

  override def passwordInfoRemove(request: PasswordInfoRemoveRequest) = GrpcErrorHandler {
    log.info(s"passwordInfoRemove - {}", request)
    require(request.loginInfo.nonEmpty, "loginInfo is empty")

    passwordInfoDao.remove(request.loginInfo.get)
      .map { _ =>
        PasswordInfoRemoveResponse(
          request.header.map(_.copy(token = ""))
        )
      }
  }
}

object PasswordInfoServiceGrpcImpl {

  implicit def passwordInfoCast(passwordInfo: util.PasswordInfo): PasswordInfoDto =
    PasswordInfoDto(passwordInfo.hasher, passwordInfo.password, passwordInfo.salt.getOrElse(""))

  implicit def passwordInfoDtoCast(passwordInfo: PasswordInfoDto): PasswordInfo =
    PasswordInfo(
      passwordInfo.hasher,
      passwordInfo.password,
      if (passwordInfo.salt.isEmpty) None else Some(passwordInfo.salt),
    )

  implicit def loginInfoDtoCast(loginInfo: LoginInfoDto): LoginInfo =
    LoginInfo(loginInfo.providerId, loginInfo.providerKey)
}