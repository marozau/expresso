package clients

import javax.inject.{Inject, Singleton}

import play.api.libs.json._

/**
  * @author im.
  */
object Quill {

  val insertTransformer: Reads[JsObject] = (__ \ 'insert).json.update(
    __.read[JsValue].map {
      case x: JsString => Json.obj("text" -> x)
      case x => x
    }
  )

  sealed trait QuillOperation

  case class Insert(text: Option[String], image: Option[String]) extends QuillOperation

  case class Attributes(link: Option[String], bold: Option[Boolean], italic: Option[Boolean]) extends QuillOperation

  case class Operation(insert: Insert, attributes: Option[Attributes]) extends QuillOperation

  implicit val textInsert: Reads[Insert] = Json.reads[Insert]
  implicit val textAttributes: Reads[Attributes] = Json.reads[Attributes]
  implicit val textOperation: Reads[Operation] = Json.reads[Operation]

  def escape(raw: String): String = {
    import scala.reflect.runtime.universe._
    Literal(Constant(raw)).toString
  }


  sealed trait Tag {
    def toTag: String
  }

  case class TextTag(text: String, bold: Boolean, italic: Boolean) extends Tag {
    override def toTag: String = s"""@_text(${escape(text)}, $bold, $italic)"""
  }

  def stringCast(value: Option[String]): String = value.fold("None")(v => s"""Some("$v")""")

  case class HrefTag(text: String, link: String, bold: Boolean, italic: Boolean) extends Tag {
    override def toTag: String = s"""@_href(${escape(text)}, "$link", $bold, $italic)"""
  }

  case class ImageTag(img: String, link: Option[String]) extends Tag {
    override def toTag: String = s"""@_image("$img",${stringCast(link)})"""
  }

}

@Singleton
class Quill @Inject()() {

  import Quill._

  def toTag(opsJson: String): List[Tag] = {
    val json = Json.parse(opsJson)

    val ops = (json \ "ops").as[JsArray].value
      .map(_.transform(insertTransformer).get)
      .map(_.as[Operation]).toList

    implicit def booleanCase(value: Option[Boolean]): Boolean = value.getOrElse(false)

    ops.map {
      case _@Operation(_@Insert(Some(text), None), None) =>
        TextTag(text, bold = false, italic = false)
      case _@Operation(_@Insert(Some(text), None), Some(attr)) => attr match {
        case _@Attributes(Some(link), _, _) =>
          HrefTag(text, link, attr.bold, attr.italic)
        case _ =>
          TextTag(text, attr.bold, attr.italic)
      }
      case _@Operation(_@Insert(None, Some(img)), None) => ImageTag(img, None)
      case _@Operation(_@Insert(None, Some(img)), Some(attr)) => ImageTag(img, attr.link)
      case _ => throw new RuntimeException("")
    }
  }

  def toTagStr(opsJson: String): String = {
    toTag(opsJson).map(_.toTag).mkString("\n")
  }
}
