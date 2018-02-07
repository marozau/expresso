package models

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import modules.UserAuthEnv
import play.api.{Configuration, Environment}
import play.api.inject.ApplicationLifecycle

/**
  * @author im.
  */
@Singleton
case class AuthContext @Inject()(
                                  silhouette: Silhouette[UserAuthEnv],
                                  credentialsProvider: CredentialsProvider,
                                  authInfoRepository: AuthInfoRepository
                                )

@Singleton
case class ApplicationContext @Inject()(
                                         config: Configuration,
                                         lifecycle: ApplicationLifecycle,
                                         playEnv: Environment,
                                         actorSystem: ActorSystem,
                                         auth: AuthContext,
                                         clock: Clock
                                       )
