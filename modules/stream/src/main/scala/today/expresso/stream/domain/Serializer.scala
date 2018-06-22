package today.expresso.stream.domain

trait Serializer[T] {
  def toBinary(t: T): Array[Byte]
  def fromBinary(bytes: Array[Byte]): T
}
