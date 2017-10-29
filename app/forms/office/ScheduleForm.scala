package forms.office

import java.time._

import play.api.data.{Form, FormError}
import play.api.data.Forms._

/**
  * @author im.
  */
object ScheduleForm {

  case class Time(hour: Int, minute: Int) {
    override def toString: String = s"${if (hour > 9) hour else "0" + hour}:${if (minute > 9) minute else "0" + minute}"

    def toLocal: LocalTime = LocalTime.of(hour, minute)
  }

  object Time {
    def apply(string: String): Time = {
      val t = string.split(":")
      Time(t.head.toInt, t.last.toInt)
    }
  }

  import play.api.data.format.Formats._
  import play.api.data.format.Formatter
  implicit object TimeFormatter extends Formatter[Time] {
    override val format = Some(("format.url", Nil))
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Time] =
      parsing(Time.apply, "error.url", Nil)(key, data)
    override def unbind(key: String, value: Time) = Map(key -> value.toString)
  }

  def nearestDate(): LocalDate = {
    val zoneOffset = ZoneOffset.ofHours(defaultTimeZone)
    val now = Instant.now().atOffset(zoneOffset).toEpochSecond
    val fire = LocalDate.now.atTime(defaultTime.hour, defaultTime.minute).toEpochSecond(zoneOffset)
    if (fire < now) LocalDate.now.plusDays(1) else LocalDate.now
  }

  val defaultTimeZone = 3
  val defaultTime = Time(6, 30)
  val defaultScheduleTime = Data(defaultTimeZone, nearestDate(), defaultTime)

  case class Data(zoneOffset: Int, date: java.time.LocalDate, time: Time) {
    lazy val toDateTime: ZonedDateTime = ZonedDateTime.of(date, time.toLocal, ZoneOffset.ofHours(zoneOffset))
  }

  val form = Form(
    mapping(
      "zoneOffset" -> number(min = -12, max = 12),
      "date" -> localDate("yyyy-MM-dd"), //TODO: validation
      "time" -> of[Time]
    )(Data.apply)(Data.unapply)
  )
}
