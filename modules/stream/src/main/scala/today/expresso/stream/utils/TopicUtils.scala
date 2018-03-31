package today.expresso.stream.utils

import com.sksamuel.avro4s.{AvroSchema, SchemaFor}

/**
  * @author im.
  */
object TopicUtils {

  def singleObjectTopic[T](implicit schemaFor: SchemaFor[T]) = AvroSchema[T].getFullName
}
