package today.expresso.stream.serde.utils

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.sksamuel.avro4s._

/**
  * @author im.
  */
object SpecificAvroUtils {

  def serialize[T : SchemaFor : ToRecord](t: T): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val output = AvroOutputStream.binary[T](baos)
    output.write(t)
    output.close()
    baos.toByteArray
  }

  def deserialize[T : SchemaFor : FromRecord](bytes: Array[Byte]): T = {
    val in = new ByteArrayInputStream(bytes)
    val input = AvroInputStream.binary[T](in)
    input.iterator.toSeq.head
  }
}
