package today.expresso.stream.avro

import java.net.URL

import com.sksamuel.avro4s.{FromValue, SchemaFor, ToSchema, ToValue}
import org.apache.avro.Schema
import org.apache.avro.Schema.Field

object URLMapping {

  implicit object URLSchemaFor extends SchemaFor[URL] {
    override def apply() = Schema.create(Schema.Type.STRING)
  }

  implicit object URLToSchema extends ToSchema[URL] {
    override val schema: Schema = Schema.create(Schema.Type.STRING)
  }

  implicit object URLToValue extends ToValue[URL] {
    override def apply(value: URL): String = value.toString
  }

  implicit object URLFromValue extends FromValue[URL] {
    override def apply(value: Any, field: Field): URL = new URL(value.toString)
  }

}
