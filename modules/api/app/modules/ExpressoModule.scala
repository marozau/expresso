package modules

import java.util.TimeZone

import com.google.inject.{AbstractModule, Provides}
import io.grpc.ManagedChannelBuilder
import play.api.Configuration
import play.api.libs.concurrent.AkkaGuiceSupport
import today.expresso.grpc.user.service.PasswordInfoServiceGrpc.PasswordInfoServiceStub
import today.expresso.grpc.user.service.{PasswordInfoServiceGrpc, UserServiceGrpc}
import today.expresso.grpc.user.service.UserServiceGrpc.UserServiceStub

/**
  * @author im.
  */
class ExpressoModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  }

  @Provides
  def provideUserServiceStub(config: Configuration): UserServiceStub = {
    val channel = ManagedChannelBuilder.forTarget(config.get[String]("grpc.user.target")).usePlaintext(true).build
    UserServiceGrpc.stub(channel).withWaitForReady()
  }

  @Provides
  def providePasswordInfoServiceStub(config: Configuration): PasswordInfoServiceStub = {
    val channel = ManagedChannelBuilder.forTarget(config.get[String]("grpc.user.target")).usePlaintext(true).build
    PasswordInfoServiceGrpc.stub(channel).withWaitForReady()
  }
}
