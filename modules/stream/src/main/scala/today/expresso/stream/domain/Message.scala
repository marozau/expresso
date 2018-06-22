package today.expresso.stream.domain

import com.sksamuel.avro4s.{FromRecord, SchemaFor, ToRecord}
import today.expresso.stream.serde.utils.SpecificAvroUtils

abstract class Message[T: SchemaFor : ToRecord : FromRecord] extends Domain {
  def toBinary(t: T): Array[Byte] = SpecificAvroUtils.serialize[T](t)
  def fromBinary(bytes: Array[Byte]): T = SpecificAvroUtils.deserialize[T](bytes)
}