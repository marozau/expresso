package models

import com.mohiva.play.silhouette.api.actions.SecuredRequest
import modules.AuthEnv
import today.expresso.grpc.user.dto.CredentialsDto

/**
  * @author im.
  */
case class Credentials(user: User, remoteAddress: String, tags: Map[String, String] = Map.empty)

object Credentials {
  implicit def fromRequest(request: SecuredRequest[AuthEnv, _]): Credentials =
    Credentials(request.identity, request.remoteAddress)

  implicit def credentialsDtoCast(credentials: Credentials): CredentialsDto = {
    import User._
    CredentialsDto(Some(credentials.user), credentials.remoteAddress, credentials.tags)
  }
}
