package today.expresso.templates.api.domain

import java.net.URL
import java.time.Instant

import play.api.libs.json.JsValue
import play.api.i18n.Lang

/**
  * @author im.
  */
case class Newsletter(id: Long,
                      userId: Long,
                      name: String,
                      locale: Lang,
                      logoUrl: Option[URL],
                      avatarUrl: Option[URL],
                      options: Option[JsValue])