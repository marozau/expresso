package api

import java.lang.invoke.MethodHandles
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

import io.grpc.{Attributes, Server, ServerBuilder, ServerTransportFilter}
import org.slf4j.LoggerFactory
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import today.expresso.grpc.user.service._

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
trait GrpcServer

@Singleton
class GrpcServerImpl @Inject()(appLifecycle: ApplicationLifecycle,
                               config: Configuration,
                               userServiceGprc: UserServiceGrpcImpl,
                               passwordInfoServiceGrpc: PasswordInfoServiceGrpcImpl)
                              (implicit ec: ExecutionContext) extends GrpcServer {
  import GrpcServerImpl._

  private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  val port = config.get[Int]("grpc.port")

  val server: Server = ServerBuilder.forPort(port)
    .addTransportFilter(filter)
    .addService(UserServiceGrpc.bindService(userServiceGprc, ec))
    .addService(PasswordInfoServiceGrpc.bindService(passwordInfoServiceGrpc, ec))
    .build
    .start

  log.info(s"gRPC server started on $port")

  appLifecycle.addStopHook { () =>
    Future {
      try {
        server.shutdown
        server.awaitTermination(5, TimeUnit.SECONDS)
      } catch {
        case t: Throwable => log.warn("gRPC server shutdown failed", t)
      }
    }
  }

}

object GrpcServerImpl {

  private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  val filter = {
    new ServerTransportFilter {
      override def transportReady(transportAttrs: Attributes) = {
        log.info(s"connected, attributes=${transportAttrs.toString}")
        super.transportReady(transportAttrs)
      }
      override def transportTerminated(transportAttrs: Attributes) = {
        log.info(s"disconnected, attributes=${transportAttrs.toString}")
        super.transportTerminated(transportAttrs)
      }
    }
  }
}
