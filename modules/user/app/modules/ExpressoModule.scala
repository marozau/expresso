package modules

import java.util.TimeZone

import api.{GrpcServer, GrpcServerImpl}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

/**
  * @author im.
  */
class ExpressoModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    bind(classOf[GrpcServer]).to(classOf[GrpcServerImpl]).asEagerSingleton()
  }
}
