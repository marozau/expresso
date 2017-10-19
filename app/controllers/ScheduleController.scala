package controllers

import java.time._
import javax.inject.{Inject, Singleton}

import models.Post
import org.quartz._
import org.quartz.core.jmx.JobDataMapSupport
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc._
import clients.{Quartz, Telegram}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

/**
  * @author im.
  */
//TODO:
// 1. create settings with timezone and convert all UI timestamps from UTC to user timezone and visa versa
// 2. get all scheduled triggers and option to fire them manually

class PostJob @Inject()(telegram: Telegram)(implicit ec: ExecutionContext) extends Job {
  override def execute(context: JobExecutionContext): Unit = {
    val data = context.getMergedJobDataMap
    val pic = data.get("picture").asInstanceOf[Option[String]]
    val msg = data.get("message").asInstanceOf[Option[String]]
    Logger.info(s"$pic, $msg")

    def send() = {
      for {
        pic <- telegram.sendPicture(pic.get)
        msg <- telegram.sendMessage(msg.get)
      } yield (pic, msg)
    }

    try {
      Logger.info(Await.result(send(), 5.seconds).toString())
    } catch {
      case t: Throwable =>
        if (context.getRefireCount > 3)
        //          Logger.error(s"stop post job=${context.getJobDetail}, reason=${t.getMessage}", t)
          throw new JobExecutionException(false) //stop job scheduling
        else
          throw t;
    }
  }
}

//case class ScheduleTime(zoneOffset: Int, date: java.time.LocalDate, hour: Int, minute: Int) {
//  lazy val dateTime: OffsetDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute)).atOffset(ZoneOffset.ofHours(zoneOffset))
//}

//case class PostForm(time: ScheduleTime, picture: Option[String], message: Option[String])

@Singleton
class ScheduleController @Inject()(components: ControllerComponents,
                                   quartz: Quartz,
                                   telegram: Telegram)(implicit ec: ExecutionContext)
  extends AbstractController(components) with I18nSupport {

//  private val scheduler = quartz.scheduler

//  def test() = Action.async {
//    for {
//    //      pic <- telegram.sendPicture("http://kindersay.com/files/images/bird.png")
//      pic <- telegram.sendPicture("AgADBAADM_41G9sdZAcsJeAQlx6uW79ToBkABGHZkulokAnHxHgAAgI")
//      msg <- telegram.sendMessage("test")
//    } yield Ok(pic.toString + msg.toString)
//  }


//  import play.api.data.Forms._
//  import play.api.data._
//
//  val postForm = Form(
//    mapping(
//      "schedule" -> mapping(
//        "zoneOffset" -> number,
//        "date" -> localDate("yyyy-MM-dd"), //TODO: validation
//        "hour" -> number(min = 0, max = 24),
//        "minutes" -> number(min = 0, max = 60)
//      )(ScheduleTime.apply)(ScheduleTime.unapply),
//      "picture" -> optional(text), //TODO: don't allow both empty pic and message
//      "message" -> optional(text)
//    )(PostForm.apply)(PostForm.unapply)
//  )
//
//  def scheduleForm() = Action { implicit request =>
//    def dateRange() = {
//      import java.time.LocalDate
//      val start = LocalDate.now()
//      (0 to 10).map(i => start.plusDays(i).toString)
//    }
//
//    Ok(views.html.user(postForm.fill(PostForm(ScheduleTime(3, LocalDate.now(), 6, 30), None, None)), dateRange().toList))
//  }
//
//  def submit = Action { implicit request =>
//
//    postForm.bindFromRequest.fold(
//      formWithErrors => {
//        BadRequest(formWithErrors.toString)
//      },
//      post => {
//        Logger.info(post.toString)
//        val date = schedulePost(Post(post.time.dateTime.withOffsetSameInstant(ZoneOffset.UTC).toInstant, post.picture, post.message))
//        Ok(date.toString)
//      }
//    )
//  }


//  // TODO: move to the service
//  // TODO: use data converters and jobs from https://github.com/enragedginger/akka-quartz-scheduler/tree/master/src/main/scala
//  private def schedulePost(post: Post) = {
//    val timstamp = post.timestamp
//    val job = JobBuilder.newJob(classOf[PostJob]).withIdentity("telegram-" + timstamp.toEpochMilli, "telegram").requestRecovery.build
//
//    val jobDataMap = Map[String, AnyRef](
//      "picture" -> post.picture,
//      "message" -> post.message
//    )
//    import scala.collection.JavaConverters._
//    val jobData = JobDataMapSupport.newJobDataMap(jobDataMap.asJava)
//
//    val trigger = TriggerBuilder.newTrigger()
//      .withIdentity("telegram-" + timstamp.toEpochMilli, "telegram")
//      .usingJobData(jobData)
//      .startAt(java.util.Date.from(timstamp))
//      .build()
//
//    // Tell quartz to schedule the job using our trigger
//    scheduler.scheduleJob(job, trigger)
//  }
}
