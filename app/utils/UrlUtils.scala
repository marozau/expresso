package utils

import scala.collection.immutable.HashMap

/**
  * @author im.
  */
object UrlUtils {

  val letters = HashMap(
    "А" -> "a",
    "Б" -> "b",
    "В" -> "v",
    "Г" -> "g",
    "Д" -> "d",
    "Е" -> "e",
    "Ё" -> "e",
    "Ж" -> "zh",
    "З" -> "z",
    "И" -> "i",
    "Й" -> "i",
    "К" -> "k",
    "Л" -> "l",
    "М" -> "m",
    "Н" -> "n",
    "О" -> "o",
    "П" -> "p",
    "Р" -> "r",
    "С" -> "s",
    "Т" -> "t",
    "У" -> "u",
    "Ф" -> "f",
    "Х" -> "h",
    "Ц" -> "c",
    "Ч" -> "ch",
    "Ш" -> "sh",
    "Щ" -> "sh",
    "Ъ" -> "",
    "Ы" -> "y",
    "Ъ" -> "",
    "Э" -> "e",
    "Ю" -> "u",
    "Я" -> "ya",
    "а" -> "a",
    "б" -> "b",
    "в" -> "v",
    "г" -> "g",
    "д" -> "d",
    "е" -> "e",
    "ё" -> "e",
    "ж" -> "zh",
    "з" -> "z",
    "и" -> "i",
    "й" -> "i",
    "к" -> "k",
    "л" -> "l",
    "м" -> "m",
    "н" -> "n",
    "о" -> "o",
    "п" -> "p",
    "р" -> "r",
    "с" -> "s",
    "т" -> "t",
    "у" -> "u",
    "ф" -> "f",
    "х" -> "h",
    "ц" -> "c",
    "ч" -> "ch",
    "ш" -> "sh",
    "щ" -> "sh",
    "ъ" -> "",
    "ы" -> "y",
    "ъ" -> "",
    "э" -> "e",
    "ю" -> "u",
    "я" -> "ya",
    " " -> "-"
  ) //TODO: remove all not allowed symbols

  def toUrl(text: String): String = {
    text.toList
      .map(_.toString)
      .map(l => letters.getOrElse(l, l))
      .mkString("")
  }
}
