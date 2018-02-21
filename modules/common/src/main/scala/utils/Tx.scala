package utils

import scala.concurrent.Future

/**
  * @author im.
  */

trait Tx[A] {
  def tx(a: A): Future[A]
}

object Tx {
  def apply[A] =
}
