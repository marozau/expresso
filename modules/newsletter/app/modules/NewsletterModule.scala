package modules

import java.util.TimeZone

import clients.{GuiceJobFactory, Quartz, QuartzFutureImpl}
import com.google.inject.AbstractModule
import org.quartz.spi.JobFactory
import play.api.libs.concurrent.AkkaGuiceSupport
import services.{MailService, MailServiceImpl, UserService, UserServiceImpl}

/**
  * @author im.
  */
class NewsletterModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    bind(classOf[JobFactory]).to(classOf[GuiceJobFactory])
    bind(classOf[Quartz]).to(classOf[QuartzFutureImpl])

    bind(classOf[UserService]).to(classOf[UserServiceImpl])
    bind(classOf[MailService]).to(classOf[MailServiceImpl])
  }
}
