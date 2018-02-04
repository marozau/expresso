package models

import java.util.UUID

import com.mohiva.play.silhouette.api.actions.SecuredRequest
import modules.AuthEnv
import play.api.Logger
import today.expresso.grpc.user.dto.CredentialsDto

/**
  * @author im.
  */
//TODO: rename to RequestContext
case class Credentials(user: User, remoteAddress: String, correlationId: UUID, tags: Map[String, String] = Map.empty)

object Credentials {
  val CORRELATION_ID_HEADER = "correlation-id"

  implicit def fromRequest(request: SecuredRequest[AuthEnv, _]): Credentials = {
    val correlationId = request.headers.get(CORRELATION_ID_HEADER).map(UUID.fromString)
      .getOrElse {
        Logger.warn(s"no correlation id for request=$request")
        UUID.randomUUID()
      }
    Credentials(request.identity, request.remoteAddress, correlationId)
  }

  implicit def credentialsDtoCast(credentials: Credentials): CredentialsDto = {
    import User._
    CredentialsDto(Some(credentials.user), credentials.remoteAddress, credentials.tags)
  }
}
