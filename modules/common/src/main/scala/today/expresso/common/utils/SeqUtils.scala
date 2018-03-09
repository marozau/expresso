package today.expresso.common.utils

/**
  * @author im.
  */
object SeqUtils {

  def insert[T](list: List[T], i: Int, value: T) = {
    val (front, back) = list.splitAt(i)
    front ++ List(value) ++ back
  }

  def moveUp[T](list: List[T], elem: T) = {
    val position = list.indexOf(elem)
    if (position == -1) throw new NoSuchElementException(s"$elem not found")
    val (f, s) = list.splitAt(position)
    insert(f, f.size - 1, elem) ++ s.tail
  }

  def moveDown[T](list: List[T], elem: T) = {
    val position = list.indexOf(elem)
    if (position == -1) throw new NoSuchElementException(s"$elem not found")
    val (f, s) = list.splitAt(position)
    f ++ insert(s.tail, 1, elem)
  }
}
