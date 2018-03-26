package today.expresso.stream.api

import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.Serializer

/**
  * @author im.
  */
trait KeySerializer extends Serializer[GenericRecord] {}
