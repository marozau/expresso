package models

import play.api.libs.json.JsValue

/**
  * @author im.
  */
case class Newsletter(id: Option[Long],
                      userId: Long,
                      name: String,
                      email: String,
                      options: Option[JsValue] = None)