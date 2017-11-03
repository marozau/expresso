package implicits

import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

import forms.newslet.{CampaignForm, ScheduleForm}
import models.Campaign

/**
  * @author im.
  */
object CampaignImplicits {

  implicit def scheduleTimeCast(s: ZonedDateTime): ScheduleForm.Data = {
    ScheduleForm.Data(TimeUnit.SECONDS.convert(s.getOffset.getTotalSeconds, TimeUnit.HOURS).toInt, s.toLocalDate, ScheduleForm.Time(s.getHour, s.getMinute))
  }
  implicit def campaignFormCast(c: Campaign): CampaignForm.Data = {
    CampaignForm.Data(c.id, c.newsletterId, c.name, c.subject, c.preview, c.fromName, c.fromEmail, c.recipientId, c.sendTime)
  }
}
