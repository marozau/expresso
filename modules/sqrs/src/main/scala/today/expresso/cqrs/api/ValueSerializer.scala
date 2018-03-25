package today.expresso.cqrs.api

import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.Serializer

/**
  * @author im.
  */
trait ValueSerializer extends Serializer[GenericRecord] {}
