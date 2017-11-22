package utils

import java.net.URL
import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.data.FormError
import play.api.mvc.Call

import scala.collection.immutable.HashMap

/**
  * @author im.
  */
@Singleton
class UrlUtils @Inject()(configuration: Configuration) {

  private val url = configuration.get[String]("domain.url")

  implicit def absoluteURL(call: Call): String =
    url + call.url + appendFragment(call)

  protected def appendFragment(call: Call) =
    if (call.fragment != null && !call.fragment.trim.isEmpty) "#" + call.fragment else ""
}

object UrlUtils {

  import play.api.data.format.Formats._
  import play.api.data.format.Formatter

  implicit object UrlFormatter extends Formatter[URL] {
    override val format = Some(("format.url", Nil))

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], URL] =
      parsing(new URL(_), "error.url", Nil)(key, data)

    override def unbind(key: String, value: URL) = Map(key -> value.toString)
  }

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
