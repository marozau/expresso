package utils

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.crypto.AuthenticatorEncoder
import com.mohiva.play.silhouette.impl.authenticators.{JWTAuthenticator, JWTAuthenticatorSettings}

import scala.util.Try

/**
  * @author im.
  */
@Singleton
class JwtTokenMarshaller @Inject()(authenticatorEncoder: AuthenticatorEncoder,
                                   settings: JWTAuthenticatorSettings) {

  def serialize(authenticator: JWTAuthenticator): String = {
    JWTAuthenticator.serialize(authenticator, authenticatorEncoder, settings)
  }

  def unserialize(token: String): Try[JWTAuthenticator] = {
    JWTAuthenticator.unserialize(token, authenticatorEncoder, settings)
  }
}
