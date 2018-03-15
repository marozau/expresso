package today.expresso.sqrs.serde.utils

import java.io.{ByteArrayOutputStream, IOException}

import org.apache.avro.Schema
import org.apache.avro.generic.{GenericDatumReader, GenericDatumWriter, GenericRecord}
import org.apache.avro.io.{DecoderFactory, EncoderFactory}


/**
  * @author im.
  */
object GenericRecordAvroUtils {

  def serialize(record: GenericRecord) = {
    val writer = new GenericDatumWriter[GenericRecord](record.getSchema)
    val out = new ByteArrayOutputStream()
    val encoder = EncoderFactory.get.binaryEncoder(out, null)
    try {
      writer.write(record, encoder)
      encoder.flush()
      out.close()
      out.toByteArray
    } catch {
      case e: IOException =>
        throw new RuntimeException("Cannot serialize object " + record, e)
    }
  }

  def deserialize(bytes: Array[Byte], schema: Schema): GenericRecord = try {
    val reader = new GenericDatumReader[GenericRecord](schema)
    val decoder = DecoderFactory.get.binaryDecoder(bytes, null)
    reader.read(null, decoder)
  } catch {
    case e: Exception =>
      throw new RuntimeException("Cannot deserialize bytes as object of class " + schema.getFullName, e)
  }
}
