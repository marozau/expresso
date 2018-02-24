package modules

import java.util.TimeZone

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import clients.{MailChimp, Quartz}

/**
  * @author im.
  */
class NewsletterModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    bind(classOf[Quartz]).asEagerSingleton()
    bind(classOf[MailChimp]).asEagerSingleton()
  }
}
