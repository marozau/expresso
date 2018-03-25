package today.expresso.cqrs.serde.utils

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.sksamuel.avro4s._

/**
  * @author im.
  */
object SpecificAvroUtils {

  def serialize[T](t: T)(implicit r: ToRecord[T], s: SchemaFor[T]): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val output = AvroOutputStream.binary[T](baos)
    output.write(t)
    output.close()
    baos.toByteArray
  }

  def deserialize[T](bytes: Array[Byte])(implicit r: FromRecord[T], s: SchemaFor[T]): T = {
    val in = new ByteArrayInputStream(bytes)
    val input = AvroInputStream.binary[T](in)
    input.iterator.toSeq.head
  }
}
