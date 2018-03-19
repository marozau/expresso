package today.expresso.sqrs.api

import org.apache.avro.generic.GenericRecord

import scala.reflect.macros.whitebox

/**
  * @author im.
  */
trait ToCaseClass[T] {
  def apply(r: GenericRecord): T
}

object ToCaseClass {
  def apply[T](r: GenericRecord)(implicit toClass: ToCaseClass[T]) = toClass(r)

  import scala.language.experimental.macros

  implicit def materializeToCaseClass[T]: ToCaseClass[T] = macro toCaseClassImpl[T]

  def toCaseClassImpl[T: c.WeakTypeTag](c: whitebox.Context): c.Expr[ToCaseClass[T]] = {
    import c.universe._
    val tType = weakTypeOf[T]
    c.Expr[ToCaseClass[T]](
      q"""
         new _root_.today.expresso.sqrs.api.ToCaseClass[$tType] {
             override def apply(r: org.apache.avro.generic.GenericRecord): $tType = com.sksamuel.avro4s.FromRecord[$tType](r)
         }
        """
    )
  }
}
