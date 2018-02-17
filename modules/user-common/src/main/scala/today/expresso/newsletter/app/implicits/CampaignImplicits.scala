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
    ScheduleForm.Data(TimeUnit.HOURS.convert(s.getOffset.getTotalSeconds, TimeUnit.SECONDS).toInt, s.toLocalDate, ScheduleForm.Time(s.getHour, s.getMinute))
  }
  implicit def campaignFormCast(c: Campaign): CampaignForm.Data = {
    CampaignForm.Data(c.id, c.newsletterId, c.editionId, c.preview, c.sendTime)
  }
}
