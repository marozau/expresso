package grpc

import io.grpc.Status

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
object GrpcErrorHandler {

  def apply[R](f: Future[R])(implicit ec: ExecutionContext): Future[R] = {
    f.recover {
      case t: Throwable =>
        throw Status.INTERNAL
          .withDescription("INTERNAL_SERVER_ERROR")
          .augmentDescription(t.getLocalizedMessage)
          .withCause(t) // This can be attached to the Status locally, but NOT transmitted to the client!
          .asRuntimeException()
    }
  }
}
