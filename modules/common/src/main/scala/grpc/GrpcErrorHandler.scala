package grpc

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import exceptions.BaseException
import io.grpc.Status

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
object GrpcErrorHandler {

  private val MESSAGE_START_INDEX: Int = 10

  def apply[R](f: Future[R])(implicit ec: ExecutionContext): Future[R] = {
    f.recover {
      case e: BaseException =>
        throw Status.INTERNAL
          .withDescription(e.code.toString)
          .augmentDescription(e.getMessage)
          .withCause(e) // This can be attached to the Status locally, but NOT transmitted to the client!
          .asRuntimeException()
      case e: ProviderException =>
        throw Status.INTERNAL
          .withDescription(BaseException.ErrorCode.INVALID_CREDENTIALS.toString)
          .augmentDescription(e.getMessage)
          .withCause(e) // This can be attached to the Status locally, but NOT transmitted to the client!
          .asRuntimeException()
      case t: Throwable =>
        throw Status.INTERNAL
          .withDescription(BaseException.ErrorCode.INTERNAL_SERVER_ERROR.toString)
          .augmentDescription(t.getMessage)
          .withCause(t) // This can be attached to the Status locally, but NOT transmitted to the client!
          .asRuntimeException()
    }
  }

  def getDescription(message: String): (String, String) = {
    message.substring(MESSAGE_START_INDEX).split('\n').toList match {
      case description :: details :: Nil => (description, details)
      case description :: Nil => (description, "")
      case _ => (message, "")
    }
  }
}
