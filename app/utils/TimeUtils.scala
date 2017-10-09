package utils

import controllers.CampaignController.Time
import play.api.data.FormError

/**
  * @author im.
  */
object TimeUtils {

  import play.api.data.format.Formats._
  import play.api.data.format.Formatter
  implicit object UrlFormatter extends Formatter[Time] {
    override val format = Some(("format.url", Nil))
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Time] =
      parsing(Time.apply, "error.url", Nil)(key, data)
    override def unbind(key: String, value: Time) = Map(key -> value.toString)
  }
}
