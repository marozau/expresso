package implicits

/**
  * @author im.
  */
object CommonImplicits {

  implicit def optionStringCast(str: Option[String]): String = str.getOrElse("")
}
