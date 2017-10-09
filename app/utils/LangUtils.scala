package utils

import scala.collection.immutable.HashMap

/**
  * @author im.
  */
object LangUtils {

  val letters = HashMap(
    "А" -> "A",
    "Б" -> "B",
    "В" -> "V",
    "Г" -> "G",
    "Д" -> "D",
    "Е" -> "E",
    "Ё" -> "E",
    "Ж" -> "ZH",
    "З" -> "Z",
    "И" -> "I",
    "Й" -> "I",
    "К" -> "K",
    "Л" -> "L",
    "М" -> "M",
    "Н" -> "N",
    "О" -> "O",
    "П" -> "P",
    "Р" -> "R",
    "С" -> "S",
    "Т" -> "T",
    "У" -> "U",
    "Ф" -> "F",
    "Х" -> "H",
    "Ц" -> "C",
    "Ч" -> "CH",
    "Ш" -> "SH",
    "Щ" -> "SH",
    "Ъ" -> "'",
    "Ы" -> "Y",
    "Ъ" -> "'",
    "Э" -> "E",
    "Ю" -> "U",
    "Я" -> "YA",
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
    "ъ" -> "'",
    "ы" -> "y",
    "ъ" -> "'",
    "э" -> "e",
    "ю" -> "u",
    "я" -> "ya"
  )

  def toTranslit(text: String): String = {
    text.toList
      .map(_.toString)
      .map(l => if (l.equals(" ")) "-" else l) //TODO: remove all not allowed symbols
      .map(l => letters.getOrElse(l, l))
      .mkString("")
  }
}
