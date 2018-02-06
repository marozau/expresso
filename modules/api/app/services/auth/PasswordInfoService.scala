package services.auth

import java.util.concurrent.ThreadLocalRandom
import javax.inject.Inject

import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.api.{LoginInfo, util}
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import today.expresso.grpc.Header
import today.expresso.grpc.user.dto.{LoginInfoDto, PasswordInfoDto}
import today.expresso.grpc.user.service._

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
object PasswordInfoService {

  implicit def passwordInfoCast(passwordInfo: PasswordInfoDto): PasswordInfo =
    PasswordInfo(
      passwordInfo.hasher,
      passwordInfo.password,
      if (passwordInfo.salt.isEmpty) None else Some(passwordInfo.salt),
    )

  implicit def passwordInfoDtoCast(passwordInfo: util.PasswordInfo): PasswordInfoDto =
    PasswordInfoDto(passwordInfo.hasher, passwordInfo.password, passwordInfo.salt.getOrElse(""))

  implicit def loginInfoCast(loginInfo: LoginInfoDto): LoginInfo =
    LoginInfo(loginInfo.providerId, loginInfo.providerKey)

  implicit def loginInfoDtoCast(loginInfo: LoginInfo): LoginInfoDto =
    LoginInfoDto(loginInfo.providerID, loginInfo.providerKey)
}

class PasswordInfoService @Inject()(passwordInfoService: PasswordInfoServiceGrpc.PasswordInfoService)(implicit ec: ExecutionContext)
  extends DelegableAuthInfoDAO[PasswordInfo] {

  import PasswordInfoService._

  /**
    * Finds the auth info which is linked with the specified login info.
    *
    * @param loginInfo The linked login info.
    * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
    */
  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    passwordInfoService.passwordInfoFind(
      PasswordInfoFindRequest(
        Some(Header(ThreadLocalRandom.current().nextInt())),
        Some(loginInfo)
      )
    )
      .map(_.passwordInfo)
      .map { dbPasswordInfoOption =>
        dbPasswordInfoOption.map(passwordInfoCast)
      }
  }

  /**
    * Adds new auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be added.
    * @param authInfo  The auth info to add.
    * @return The added auth info.
    */
  def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    passwordInfoService.passwordInfoAdd(
      PasswordInfoAddRequest(
        Some(Header(ThreadLocalRandom.current().nextInt())),
        Some(loginInfo),
        Some(authInfo)
      )
    )
      .map(_.passwordInfo.get)
  }

  /**
    * Updates the auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be updated.
    * @param authInfo  The auth info to update.
    * @return The updated auth info.
    */
  def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    passwordInfoService.passwordInfoUpdate(
      PasswordInfoUpdateRequest(
        Some(Header(ThreadLocalRandom.current().nextInt())),
        Some(loginInfo),
        Some(authInfo)
      )
    )
      .map(_.passwordInfo.get)

  /**
    * Saves the auth info for the given login info.
    *
    * This method either adds the auth info if it doesn't exists or it updates the auth info
    * if it already exists.
    *
    * @param loginInfo The login info for which the auth info should be saved.
    * @param authInfo  The auth info to save.
    * @return The saved auth info.
    */
  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    passwordInfoService.passwordInfoSave(
      PasswordInfoSaveRequest(
        Some(Header(ThreadLocalRandom.current().nextInt())),
        Some(loginInfo),
        Some(authInfo)
      )
    )
      .map(_.passwordInfo.get)
  }

  /**
    * Removes the auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be removed.
    * @return A future to wait for the process to be completed.
    */
  def remove(loginInfo: LoginInfo): Future[Unit] =
    passwordInfoService.passwordInfoRemove(
      PasswordInfoRemoveRequest(
        Some(Header(ThreadLocalRandom.current().nextInt())),
        Some(loginInfo)
      )
    ).map(_ => Unit)
}
