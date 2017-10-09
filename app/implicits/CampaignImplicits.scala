package implicits

import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

import controllers.CampaignController.{CampaignForm, ScheduleTime, Time}
import models.Campaign

/**
  * @author im.
  */
object CampaignImplicits {

  implicit def scheduleTimeCast(s: ZonedDateTime): ScheduleTime = {
    ScheduleTime(TimeUnit.SECONDS.convert(s.getOffset.getTotalSeconds, TimeUnit.HOURS).toInt, s.toLocalDate, Time(s.getHour, s.getMinute))
  }
  implicit def campaignFormCast(c: Campaign): CampaignForm = {
    CampaignForm(c.id, c.newsletterId, c.name, c.subject, c.preview, c.fromName, c.fromEmail, c.recipientId, c.sendTime)
  }
}
