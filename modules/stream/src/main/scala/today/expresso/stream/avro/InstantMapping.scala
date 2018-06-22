package today.expresso.stream.avro

import java.time.Instant

import com.sksamuel.avro4s.{FromValue, SchemaFor, ToSchema, ToValue}
import org.apache.avro.Schema
import org.apache.avro.Schema.Field

object InstantMapping {

  implicit object InstantSchemaFor extends SchemaFor[Instant] {
    override def apply() = Schema.create(Schema.Type.LONG)
  }

  implicit object InstantToSchema extends ToSchema[Instant] {
    override val schema: Schema = Schema.create(Schema.Type.LONG)
  }

  implicit object InstantToValue extends ToValue[Instant] {
    override def apply(value: Instant): Long = value.toEpochMilli
  }

  implicit object InstantFromValue extends FromValue[Instant] {
    override def apply(value: Any, field: Field): Instant = Instant.ofEpochMilli(value.asInstanceOf[Long])
  }

}
