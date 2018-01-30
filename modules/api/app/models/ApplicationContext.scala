package models

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import com.mohiva.play.silhouette.api.Silhouette
import modules.AuthEnv
import play.api.{Configuration, Environment}
import play.api.inject.ApplicationLifecycle

/**
  * @author im.
  */
@Singleton
case class ApplicationContext @Inject()(
                                         config: Configuration,
                                         lifecycle: ApplicationLifecycle,
                                         playEnv: Environment,
                                         actorSystem: ActorSystem,
                                         silhouette: Silhouette[AuthEnv],
                                       )
