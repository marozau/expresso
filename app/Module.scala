import java.io.IOException
import java.util.TimeZone
import java.util.jar.Manifest

import com.google.inject.AbstractModule
import play.api.Logger
import play.api.libs.concurrent.AkkaGuiceSupport
import services.{Elasticsearch, MailChimp, Quartz}

/**
  * @author im.
  */
class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    bind(classOf[Quartz]).asEagerSingleton()
    bind(classOf[MailChimp]).asEagerSingleton()
    bind(classOf[Elasticsearch]).asEagerSingleton()
  }
}
