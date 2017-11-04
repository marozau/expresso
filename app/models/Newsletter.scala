package models

import play.api.libs.json.JsValue

/**
  * @author im.
  */
case class Newsletter(id: Option[Long],
                      userId: Long,
                      email: String,
                      name: String,
                      options: Option[JsValue] = None)