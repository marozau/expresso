package today.expresso.sqrs.api

import org.apache.avro.generic.GenericRecord

import scala.reflect.macros.whitebox

/**
  * @author im.
  */
trait ToValueRecord[T] {
  def apply(t: T): GenericRecord
}

object ToValueRecord {
  def apply[T](t: T)(implicit toValue: ToValueRecord[T]) = toValue(t)

  import scala.language.experimental.macros

  implicit def materializeToValue[T]: ToValueRecord[T] = macro toValueImpl[T]

  def toValueImpl[T: c.WeakTypeTag](c: whitebox.Context): c.Expr[ToValueRecord[T]] = {
    import c.universe._
    val tType = weakTypeOf[T]
    c.Expr[ToValueRecord[T]](
      q"""
         new _root_.today.expresso.sqrs.api.ToValueRecord[$tType] {
             override def apply(t: $tType) = com.sksamuel.avro4s.ToRecord[$tType](t)
         }
        """
    )
  }
}