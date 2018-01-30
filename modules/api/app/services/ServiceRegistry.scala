package services

import javax.inject.{Inject, Singleton}

import services.user.UserService

/**
  * @author im.
  */
@Singleton
case class ServiceRegistry @Inject() (
                                       userService: UserService
                                     )
