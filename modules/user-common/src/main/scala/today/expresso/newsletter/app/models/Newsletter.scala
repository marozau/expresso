package models

import java.net.URL
import java.time.Instant

import play.api.libs.json.JsValue
import play.api.i18n.Lang

/**
  * @author im.
  */
//TODO: nameUrl will be used in archive component

case class Newsletter(id: Long,
                      userId: Long,
                      name: String,
                      locale: Lang,
                      logoUrl: Option[URL],
                      avatarUrl: Option[URL],
                      options: Option[JsValue],
                      createdTimestamp: Instant)