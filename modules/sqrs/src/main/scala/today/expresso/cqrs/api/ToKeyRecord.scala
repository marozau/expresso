package today.expresso.cqrs.api

import com.sksamuel.avro4s.TypeHelper
import org.apache.avro.generic.GenericRecord

import scala.reflect.macros.whitebox

/**
  * @author im.
  */
trait ToKeyRecord[T] {
  def apply(t: T): GenericRecord
}

// it's possible to use scala macros to implement implicit ToKey[T] using @Key annotation
object ToKeyRecord {
  def apply[T: KeyRecord](t: T)(implicit toKey: ToKeyRecord[T]) = toKey(t)

  import scala.language.experimental.macros

  implicit def materializeToKey[T]: ToKeyRecord[T] = macro toKeyImpl[T]

  def toKeyImpl[T: c.WeakTypeTag](c: whitebox.Context): c.Expr[ToKeyRecord[T]] = {
    import c.universe._
    val helper = TypeHelper(c)
    val tType = weakTypeOf[T]
    //TODO: check if userId exists. Maybe it's better to create specific default value to as key?
    //TODO: annotate with key and use this field as t.<filedName>
    val fields = helper.fieldsOf(tType)

    def isKeyRecordAnnotation(sym: Symbol): Boolean = sym.annotations.exists { a =>
      a.tree.tpe == typeOf[today.expresso.cqrs.api.Key]
    }

    val fieldOption = fields.zipWithIndex
      .map { case ((f, sig), index) =>
        (f, isKeyRecordAnnotation(f))
      }.find(_._2 == true)

    if (fieldOption.isEmpty)
      c.abort(c.enclosingPosition, "class does not have @Key annotation, you should provide implicit ToKeyRecord")

    val field = fieldOption.get._1
    val termName = TermName(field.name.decodedName.toString.trim)
    val fType = field.typeSignature

    c.Expr[ToKeyRecord[T]](
      q"""
         new _root_.today.expresso.cqrs.api.ToKeyRecord[$tType] {
             override def apply(t: $tType) = com.sksamuel.avro4s.ToRecord[_root_.today.expresso.cqrs.api.KeyRecord[$fType]](_root_.today.expresso.cqrs.api.KeyRecord[$fType](t.$termName))
         }
        """
    )
  }
}