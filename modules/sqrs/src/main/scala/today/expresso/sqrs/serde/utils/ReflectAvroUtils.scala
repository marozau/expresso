package today.expresso.sqrs.serde.utils

import java.io.{ByteArrayOutputStream, IOException}

import org.apache.avro.Schema
import org.apache.avro.io.{DecoderFactory, EncoderFactory}
import org.apache.avro.reflect.{ReflectData, ReflectDatumReader, ReflectDatumWriter}

import scala.reflect.ClassTag


/**
  * @author im.
  */
object ReflectAvroUtils {

  def serialize[T](data: Any)(implicit ct: ClassTag[T]) = {
    val schema = ReflectData.get.getSchema(data.getClass)
    val writer = new ReflectDatumWriter[T](schema)
    val out = new ByteArrayOutputStream()
    val encoder = EncoderFactory.get.binaryEncoder(out, null)
    try {
      writer.write(data.asInstanceOf[T], encoder)
      encoder.flush()
      out.close()
      out.toByteArray
    } catch {
      case e: IOException =>
        throw new RuntimeException("Cannot serialize object " + data, e)
    }
  }

  /**
    * Deserialize an object of specified class from given array of bytes.
    *
    * WARNING: This is a low-level method that only deserializes an object.
    * It's NOT COMPATIBLE with Confluent's message format, to read messages from Kafka
    * use ReflectKafkaAvroSerializer instead.
    **/
  def deserialize[T](bytes: Array[Byte], schema: Schema)(implicit c: ClassTag[T]): T = try {
    val reader = new ReflectDatumReader[T](schema)
    val decoder = DecoderFactory.get.binaryDecoder(bytes, null)
    val datum: T = Instantiator.newCase[T]()
    reader.read(datum, decoder)
  } catch {
    case e: Exception =>
      throw new RuntimeException("Cannot deserialize bytes as object of class " + schema.getFullName, e)
  }
}

object Instantiator {

  import reflect._
  import scala.reflect.runtime.universe._
  import scala.reflect.runtime.{currentMirror => cm}

  def newCase[A]()(implicit t: ClassTag[A]): A = {
    val claas = cm classSymbol t.runtimeClass
    val modul = claas.companionSymbol.asModule
    val im = cm reflect (cm reflectModule modul).instance
    defaut[A](im, "apply")
  }

  def defaut[A](im: InstanceMirror, name: String): A = {
    val at = newTermName(name)
    val ts = im.symbol.typeSignature
    val method = (ts member at).asMethod

    // either defarg or default val for type of p
    def valueFor(p: Symbol, i: Int): Any = {
      val defarg = ts member newTermName(s"$name$$default$$${i + 1}")
      if (defarg != NoSymbol) {
        (im reflectMethod defarg.asMethod) ()
      } else {
        p.typeSignature match {
          case t if t =:= typeOf[String] => null
          case t if t =:= typeOf[Int] => 0
          case t if t =:= typeOf[Long] => 0L
          case x => throw new IllegalArgumentException(x.toString)
        }
      }
    }

    val args = (for (ps <- method.paramss; p <- ps) yield p).zipWithIndex map (p => valueFor(p._1, p._2))
    (im reflectMethod method) (args: _*).asInstanceOf[A]
  }
}
