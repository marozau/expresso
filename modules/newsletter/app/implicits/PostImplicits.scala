package implicits

import java.net.URL

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import models.Post
import play.api.libs.json.{JsSuccess, JsValue, Json, Reads}

/**
  * @author im.
  */
object PostImplicits {

  final val emptyOptions: JsValue = Json.parse("{}")

  implicit def postIdsCast(posts: List[Post]): List[Long] = posts.map(_.id)

  implicit def urlCast(url: String): URL = new URL(url)

  implicit def urlListCast(urls: List[String]): List[URL] = urls.map(urlCast)

  implicit def urlStringCast(urls: URL): String = urls.toString

  implicit def urlStringListCast(urls: List[URL]): List[String] = urls.map(urlStringCast)

  implicit def optionUrlStringCast(url: Option[URL]): Option[String] = url.map(urlStringCast)

  implicit def optionUrlCast(url: Option[String]): Option[URL] = url.map(urlCast)

  val reader = new ObjectMapper().readerFor(new TypeReference[Map[String, Any]]() {})
  implicit val mapReads: Reads[Map[String, Any]] = (jv: JsValue) => JsSuccess(reader.readValue(jv.toString))

  implicit def jsonOptionsCast(options: Option[JsValue]): Map[String, Any] = {
    Map.empty
//    emptyOptions
//    options.getOrElse(emptyOptions).as[Map[String, Any]]
  }
}
