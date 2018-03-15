import java.io.ByteArrayOutputStream

import com.sksamuel.avro4s.SchemaFor
import today.expresso.sqrs.event.newsletter.NewsletterEditionSent

import scala.reflect.ClassTag

val string = "test"

/*def serialize[T](t: T)(implicit ct: ClassTag[T]) = {
  implicit val schemaFor: SchemaFor[T] = SchemaFor[T]
  schemaFor
}*/

import reflect.runtime.universe
import reflect.runtime.universe._

def test1() {
  val tType = weakTypeOf[String]
  println(tType.typeSymbol.isClass)
}
test1()


def test2[T](t: T) = {
  val tType = weakTypeOf[T]
  println(tType.typeSymbol.isClass)
}
test2()

import org.apache.avro.Schema
import org.apache.avro.io.BinaryEncoder
import org.apache.avro.io.DatumWriter
import org.apache.avro.io.EncoderFactory
import org.apache.avro.reflect.ReflectData
import org.apache.avro.reflect.ReflectDatumWriter

def test3[T](t: scala.Any)(implicit ct: ClassTag[T]) = {
  val schema = ReflectData.get.getSchema(t.getClass)
  println(schema)
  val writer = new ReflectDatumWriter[T](schema)
  val out = new ByteArrayOutputStream()
  val encoder = EncoderFactory.get.binaryEncoder(out, null)
}

val t = NewsletterEditionSent(1, 1, 1, 1)
//case class Test(v1: Long, v2: String)
//val t = Test(1, "test")
test3(t)

println(Option[String].typeSignature)

