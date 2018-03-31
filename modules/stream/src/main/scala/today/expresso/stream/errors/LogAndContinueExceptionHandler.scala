package today.expresso.stream.errors

import java.util

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.streams.errors.ProductionExceptionHandler
import org.apache.kafka.streams.errors.ProductionExceptionHandler.ProductionExceptionHandlerResponse
import play.api.Logger

/**
  * @author im.
  */
class LogAndContinueExceptionHandler extends ProductionExceptionHandler {
  override def handle(record: ProducerRecord[Array[Byte], Array[Byte]], exception: Exception) = {
    Logger.error(s"Exception caught during produce a result, record = ${record.toString}, continue processing", exception)
    ProductionExceptionHandlerResponse.CONTINUE
  }

  override def configure(configs: util.Map[String, _]) = {
    // ignore
  }
}
