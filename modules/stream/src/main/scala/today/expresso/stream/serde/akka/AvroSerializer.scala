package today.expresso.stream.serde.akka

import akka.serialization.Serializer
import today.expresso.stream.domain.Domain

object AvroSerializer {

  //TODO: cache
  def getCompanion(manifest: Class[_]): Any = {
    import scala.reflect.runtime.{currentMirror => cm, _}

    val rootMirror = universe.runtimeMirror(manifest.getClassLoader)
    val classSymbol = rootMirror.classSymbol(manifest)
    val companionModule = classSymbol.companion.asModule
    val companionMirror = cm.reflectModule(companionModule)
    companionMirror.instance
  }
}

class AvroSerializer extends Serializer {

  import AvroSerializer._

  override def identifier = 87

  override def includeManifest = true

  override def toBinary(o: AnyRef) = o match {
    case _: Domain =>
      val companion = getCompanion(o.getClass)
      val methodSerialize = companion.getClass.getDeclaredMethod("toBinary", o.getClass)
      methodSerialize.invoke(companion, o).asInstanceOf[Array[Byte]]
    case _ => throw new IllegalArgumentException("Can't serialize a non-avro message using avro [" + o + "]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]) = {
    manifest match {
      case None => throw new IllegalArgumentException("Need a avro message class to be able to serialize bytes using avro")
      case Some(c) =>
        val companion = getCompanion(c)
        val methodDeserialize = companion.getClass.getDeclaredMethod("fromBinary", classOf[Array[Byte]])
        methodDeserialize.invoke(companion, bytes)
    }
  }
}
