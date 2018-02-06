package modules

import java.util.TimeZone

import com.google.inject.{AbstractModule, Provides}
import io.grpc.ManagedChannelBuilder
import play.api.Configuration
import play.api.libs.concurrent.AkkaGuiceSupport
import today.expresso.grpc.user.service.PasswordInfoServiceGrpc.PasswordInfoService
import today.expresso.grpc.user.service.UserIdentityServiceGrpc.UserIdentityService
import today.expresso.grpc.user.service.UserServiceGrpc.UserService
import today.expresso.grpc.user.service.{PasswordInfoServiceGrpc, UserIdentityServiceGrpc, UserServiceGrpc}

/**
  * @author im.
  */
class ExpressoModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  }

  @Provides
  def provideUserIdentityServiceStub(config: Configuration): UserIdentityService = {
    val channel = ManagedChannelBuilder.forTarget(config.get[String]("grpc.user.target")).usePlaintext(true).build
    UserIdentityServiceGrpc.stub(channel).withWaitForReady()
  }


  @Provides
  def providePasswordInfoServiceStub(config: Configuration): PasswordInfoService = {
    val channel = ManagedChannelBuilder.forTarget(config.get[String]("grpc.user.target")).usePlaintext(true).build
    PasswordInfoServiceGrpc.stub(channel).withWaitForReady()
  }

  @Provides
  def provideUserServiceStub(config: Configuration): UserService = {
    val channel = ManagedChannelBuilder.forTarget(config.get[String]("grpc.user.target")).usePlaintext(true).build
    UserServiceGrpc.stub(channel).withWaitForReady()
  }

}
