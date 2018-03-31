package today.expresso.stream.api

/**
  * @author im.
  */
trait ToEvent[T, R] {
  def apply(t: T): R
}

object ToEvent {
  def apply[T, R](t: T)(implicit toEvent: ToEvent[T, R]) = toEvent(t)
}
