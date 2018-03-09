package today.expresso.common.utils

import scala.concurrent.Future

/**
  * @author im.
  */

trait Tx[A] {
  def tx(a: A): Future[Any]
}
