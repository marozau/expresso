package today.expresso.stream.avro

import com.sksamuel.avro4s.{FromValue, SchemaFor, ToSchema, ToValue}
import org.apache.avro.Schema
import org.apache.avro.Schema.Field
import play.api.libs.json.{JsValue, Json}

object JsValueMapping {

  implicit object JsValueSchemaFor extends SchemaFor[JsValue] {
    override def apply() = Schema.create(Schema.Type.STRING)
  }

  implicit object JsValueToSchema extends ToSchema[JsValue] {
    override val schema: Schema = Schema.create(Schema.Type.STRING)
  }

  implicit object JsValueToValue extends ToValue[JsValue] {
    override def apply(value: JsValue): String = Json.stringify(value)
  }

  implicit object JsValueFromValue extends FromValue[JsValue] {
    override def apply(value: Any, field: Field): JsValue = Json.toJson(value.toString)
  }

}
