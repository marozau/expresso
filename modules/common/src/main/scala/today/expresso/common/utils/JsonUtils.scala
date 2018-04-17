package today.expresso.common.utils

import play.api.libs.json.{JsError, JsResult}

object JsonUtils {

  implicit def jsonResult[A](result: JsResult[A]): A = result match {
    case e: JsError => throw new RuntimeException("failed to parse json, error=" + e)
    case r: JsResult[_] => r.get
  }
}
