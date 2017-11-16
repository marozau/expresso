package models

import java.net.URL

import play.api.libs.json.JsValue
import play.api.i18n.Lang

/**
  * @author im.
  */
case class Newsletter(id: Option[Long],
                      userId: Long,
                      name: String,
                      nameUrl: String,
                      email: String,
                      lang: Lang,
                      logo: Option[URL],
                      options: Option[JsValue] = None)